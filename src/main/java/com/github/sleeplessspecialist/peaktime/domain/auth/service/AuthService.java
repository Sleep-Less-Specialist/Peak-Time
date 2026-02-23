package com.github.sleeplessspecialist.peaktime.domain.auth.service;

import java.time.Duration;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Instant;
import java.util.UUID;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LoginReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.token.RefreshTokenStore;
import com.github.sleeplessspecialist.peaktime.domain.auth.token.SessionEntry;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserRole;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserStatus;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtProperties;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenProvider;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증(Auth) 도메인의 비즈니스 로직을 처리하는 서비스입니다.
 *
 * <p>
 * 회원가입, 로그인 등 인증 과정에서 필요한 검증/저장 로직을 담당합니다.
 * 회원가입 시 휴대폰 번호를 정규화(숫자만 저장)하고, 중복 이메일을 검증합니다.
 * 회원가입이 성공하면 회원가입 보너스 포인트를 지급하고, 포인트 변동 이력을 기록합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 24.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final PointService pointService;
	private final JwtTokenProvider jwtTokenProvider;
	private final StringRedisTemplate stringRedisTemplate;
	private final JwtProperties jwtProperties;
	private final RefreshTokenStore refreshTokenStore;

	private static final int MAX_LOGIN_ATTEMPTS = 5;
	private static final Duration LOGIN_ATTEMPT_WINDOW_TTL = Duration.ofMinutes(10);
	private static final Duration LOGIN_LOCK_TTL = Duration.ofMinutes(10);

	/**
	 * 회원가입을 처리합니다.
	 * <p>
	 * 이메일 중복을 사전 검증하고, 비밀번호는 해시(Bcrypt)하여 저장합니다.
	 * 휴대폰 번호는 숫자만 남기도록 정규화한 뒤 저장합니다.
	 * 동시성으로 인해 DB 유니크 제약 위반이 발생하는 경우에도 동일한 에러 코드로 변환합니다.
	 * </p>
	 *
	 * @param request 회원가입 요청 DTO
	 * @return 생성된 사용자 식별자와 기본 정보를 포함한 응답 DTO
	 * @throws CustomException 이메일 중복 또는 휴대폰 번호 정책 위반 시
	 */
	@Transactional
	public SignupRes signup(SignupReq request) {

		final String email = normalizeEmail(request.getEmail());
		validateEmailNotExists(email);

		String encodedPassword = passwordEncoder.encode(request.getPassword());
		String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());

		User user = User.createForSignup(
			request.getName(),
			email,
			encodedPassword,
			normalizedPhoneNumber
		);

		final User saved = saveUserOrThrowDuplicateEmail(user, email);

		pointService.grantSignupBonus(saved);
		log.info("회원가입 완료 - email: {}, userId: {}", saved.getEmail(), saved.getId());
		return new SignupRes(saved.getId(), saved.getEmail(), saved.getName());
	}

	private String normalizePhoneNumber(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}

		String normalized = phoneNumber.replaceAll("\\D", "");

		if (normalized.length() != 10 && normalized.length() != 11) {
			log.debug(
				"휴대폰 번호 정규화 저장에 실패했습니다. raw={}, normalized={}",
				phoneNumber,
				normalized
			);
			throw new CustomException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}

		return normalized;
	}

	/**
	 * 로그인을 처리합니다.
	 * <p>
	 * 이메일 / 비밀번호를 검증한 뒤 Access / Refresh Token을 발급합니다.
	 * Refresh Token은 서버에서 생성한 세션 식별자(sid, UUID)를 포함하며, Redis 화이트리스트를 sid 단위로 저장하고 TTL로 만료를 관리합니다.
	 * </p>
	 *
	 * <p>
	 * 인증 실패(이메일 없음/비밀번호 불일치)는 401로 통일하여 반환합니다.
	 * 로그인 시도 횟수 제한(브루트포스 방지) 위반은 429로 처리합니다.
	 * 멀티 디바이스/동시 로그인 제어(단일 세션 강제 등)는 MVP 범위에서 제외하고, RefreshToken을 화이트리스트로 누적 관리합니다.
	 * </p>
	 *
	 * @param request 로그인 요청 DTO
	 * @return 컨트롤러에 전달할 토큰 묶음(TokenBundle)
	 * @throws CustomException 인증 실패 또는 보안 정책 위반 시
	 */
	public TokenBundle login(final LoginReq request) {
		final String email = normalizeEmail(request.getEmail());

		validateLoginAttemptAllowed(email);

		try {
			final User user = findUserOrThrowInvalidCredentials(email);
			validateUserIsActive(user);
			validatePasswordMatches(request.getPassword(), user.getPasswordHash());

			final Long userId = user.getId();
			final String role = resolveRole(user.getRole());

			final String accessToken = jwtTokenProvider.createAccessToken(userId, role);
			final String sessionId = UUID.randomUUID().toString().replace("-", "");
			final String refreshToken = jwtTokenProvider.createRefreshToken(userId, sessionId);
			saveRefreshTokenWhitelist(userId, sessionId, refreshToken);

			clearLoginAttempts(email);

			return new TokenBundle(
				accessToken,
				refreshToken,
				"Bearer",
				accessTokenExpiresInSeconds()
			);
		} catch (CustomException e) {
			if (e.getErrorCode() == AuthErrorCode.INVALID_CREDENTIALS) {
				recordLoginFailure(email);
			}
			throw e;
		}
	}

	/**
	 * HttpOnly Cookie로 전달된 Refresh Token을 이용해 Access Token을 재발급합니다.
	 *
	 * <p>
	 * Refresh Token의 서명/만료를 검증하고, Redis 화이트리스트에 등록된 토큰인지 확인합니다.
	 * 검증이 완료되면 기존 Refresh Token을 즉시 폐기하고 새로운 Access / Refresh Token을 발급합니다. (Refresh Token Rotation)
	 * 동시 요청 상황에서도 안전하도록 원자적 회전(rotateIfMatch)으로 처리합니다.
	 * </p>
	 *
	 * <p>
	 * 이미 폐기된 Refresh Token(jti 재사용)이 재사용되면 Refresh Reuse Detection으로 간주하고 요청을 차단합니다.
	 * </p>
	 *
	 * @param refreshToken 재발급에 사용할 Refresh Token
	 * @return 새로 발급된 토큰 묶음(TokenBundle)
	 * @throws CustomException 유효하지 않거나 만료된 Refresh Token인 경우
	 */
	public TokenBundle reissue(final String refreshToken) {
		validateRefreshToken(refreshToken);

		// 토큰에서 필요한 값들을 한 번만 추출 (중복 파싱 방지)
		final Long userId = jwtTokenProvider.getUserId(refreshToken);
		final String sid = jwtTokenProvider.getSessionId(refreshToken);
		final String jti = jwtTokenProvider.getJti(refreshToken);

		// RTR - Refresh Token 재사용 탐지 (jti 기반)
		final String usedKey = "rt:used:" + jti;
		Boolean reused = stringRedisTemplate.hasKey(usedKey);
		if (Boolean.TRUE.equals(reused)) {
			// 재사용 감지 시 해당 세션을 즉시 폐기
			revokeRefreshTokenWhitelist(userId, sid);
			throw new CustomException(AuthErrorCode.REFRESH_REUSED);
		}

		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		validateRefreshTokenWhitelisted(userId, sid, refreshToken);

		return rotateAndIssueTokens(user, sid, refreshToken);
	}

	private void validateRefreshToken(final String refreshToken) {
		try {
			jwtTokenProvider.validateToken(refreshToken);
		} catch (Exception e) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}
	}

	/**
	 * Refresh Token 기반 로그아웃을 처리합니다.
	 * <p>
	 * 전달받은 Refresh Token 유효성 검증 이후 Redis 화이트리스트에서 해당 토큰을 삭제하여 토큰 재발급을 차단합니다.
	 * Access Token은 Stateless(JWT) 특성상 서버에 저장되지 않으므로, 로그아웃 이후에도 만료 시점까지는 유효할 수 있습니다.
	 * </p>
	 *
	 * @param refreshToken 로그아웃 대상 Refresh Token
	 * @throws CustomException 유효하지 않은 Refresh Token 인 경우
	 */
	public void logout(final String refreshToken) {
		validateRefreshToken(refreshToken);

		final User user = validateUserByRefreshToken(refreshToken);
		final Long userId = user.getId();

		final String sid = jwtTokenProvider.getSessionId(refreshToken);
		validateRefreshTokenWhitelisted(userId, sid, refreshToken);
		revokeRefreshTokenWhitelist(userId, sid);
	}

	private User validateUserByRefreshToken(final String refreshToken) {
		final Long userId;
		try {
			userId = jwtTokenProvider.getUserId(refreshToken);
		} catch (Exception e) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		final User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		return user;
	}

	private TokenBundle rotateAndIssueTokens(
		final User user,
		final String sid,
		final String oldRefreshToken) {
		final Long userId = user.getId();
		final String role = resolveRole(user.getRole());

		final String newAccessToken = jwtTokenProvider.createAccessToken(userId, role);
		final String newRefreshToken = jwtTokenProvider.createRefreshToken(userId, sid);

		// RTR - 원자적 회전을 위해 기존 해시와 신규 해시를 비교 후 교체
		final String oldHash = sha256(oldRefreshToken);
		final String newHash = sha256(newRefreshToken);
		final Duration ttl = Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs());

		boolean rotated = refreshTokenStore.rotateIfMatch(
			userId,
			sid,
			oldHash,
			newHash,
			ttl
		);

		if (!rotated) {
			// 동시 요청 또는 토큰 불일치 → 재사용 또는 레이스로 간주
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		// RTR - 사용 완료된 Refresh Token의 jti 마킹
		final String oldJti = jwtTokenProvider.getJti(oldRefreshToken);
		final String usedKey = "rt:used:" + oldJti;
		stringRedisTemplate.opsForValue().set(usedKey, sid, ttl);

		return new TokenBundle(
			newAccessToken,
			newRefreshToken,
			"Bearer",
			accessTokenExpiresInSeconds()
		);
	}

	private void validateEmailNotExists(final String email) {
		if (userRepository.existsByEmail(email)) {
			throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	private User saveUserOrThrowDuplicateEmail(final User user, final String email) {
		try {
			return userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			log.debug("회원가입 저장 중 이메일 중복(유니크 제약)으로 실패했습니다. email={}", email);
			throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	private User findUserOrThrowInvalidCredentials(final String email) {
		return userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_CREDENTIALS));
	}

	private void validateUserIsActive(final User user) {
		if (user.getStatus() != UserStatus.ACTIVE) {
			throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
		}
	}

	private void validatePasswordMatches(final String rawPassword, final String passwordHash) {
		if (!passwordEncoder.matches(rawPassword, passwordHash)) {
			throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
		}
	}

	private String resolveRole(final UserRole userRole) {
		return (userRole == null) ? UserRole.STUDENT.name() : userRole.name();
	}

	private int accessTokenExpiresInSeconds() {
		final long accessTokenExpirationMs = jwtProperties.getAccessTokenExpirationMs();
		return (int)(accessTokenExpirationMs / 1000);
	}

	private void validateRefreshTokenWhitelisted(final Long userId, final String sid, final String refreshToken) {
		final String refreshTokenHash = sha256(refreshToken);

		try {
			final SessionEntry entry = refreshTokenStore.find(userId, sid)
				.orElseThrow(() -> new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN));

			if (!refreshTokenHash.equals(entry.getRefreshTokenHash())) {
				throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
			}
		} catch (RedisConnectionFailureException e) {
			log.error("Redis 장애로 RefreshToken 화이트리스트 검증에 실패했습니다. userId={}, sid={}", userId, sid, e);
			throw e;
		}
	}

	private void revokeRefreshTokenWhitelist(final Long userId, final String sid) {
		try {
			refreshTokenStore.delete(userId, sid);
		} catch (RedisConnectionFailureException e) {
			log.error("Redis 장애로 RefreshToken 화이트리스트 삭제에 실패했습니다. userId={}, sid={}", userId, sid, e);
			throw e;
		}
	}

	private void saveRefreshTokenWhitelist(final Long userId, final String sid, final String refreshToken) {
		final Instant now = Instant.now();
		final String refreshTokenHash = sha256(refreshToken);
		final Duration ttl = Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs());

		try {
			refreshTokenStore.save(new SessionEntry(userId, sid, refreshTokenHash, now, ttl));
		} catch (RedisConnectionFailureException e) {
			log.error("Redis 장애로 RefreshToken 화이트리스트 저장에 실패했습니다. userId={}, sid={}", userId, sid, e);
			throw e;
		}
	}

	private void validateLoginAttemptAllowed(final String email) {
		final String lockKey = buildLoginLockKey(email);
		try {
			final Boolean locked = stringRedisTemplate.hasKey(lockKey);
			if (Boolean.TRUE.equals(locked)) {
				throw new CustomException(AuthErrorCode.TOO_MANY_ATTEMPTS);
			}
		} catch (RedisConnectionFailureException e) {
			log.warn("Redis 장애로 로그인 시도 제한을 건너뜁니다. email={}, key={}", email, lockKey);
		}
	}

	private void recordLoginFailure(final String email) {
		final String attemptKey = buildLoginAttemptKey(email);
		try {
			final Long attempts = stringRedisTemplate.opsForValue().increment(attemptKey);

			if (attempts != null && attempts == 1L) {
				stringRedisTemplate.expire(attemptKey, LOGIN_ATTEMPT_WINDOW_TTL);
			}

			if (attempts != null && attempts >= MAX_LOGIN_ATTEMPTS) {
				final String lockKey = buildLoginLockKey(email);
				final Boolean locked = stringRedisTemplate.opsForValue().setIfAbsent(lockKey, "1", LOGIN_LOCK_TTL);
				if (Boolean.TRUE.equals(locked)) {
					log.warn("로그인 시도 횟수 초과로 계정을 잠금 처리했습니다. email={}, attempts={}", email, attempts);
				}
				throw new CustomException(AuthErrorCode.TOO_MANY_ATTEMPTS);
			}
		} catch (RedisConnectionFailureException e) {
			log.warn("Redis 장애로 로그인 실패 카운트를 기록하지 못했습니다. email={}, key={}", email, attemptKey);
		}
	}

	private void clearLoginAttempts(final String email) {
		try {
			stringRedisTemplate.delete(buildLoginAttemptKey(email));
			stringRedisTemplate.delete(buildLoginLockKey(email));
		} catch (RedisConnectionFailureException e) {
			log.warn("Redis 장애로 로그인 시도 카운트를 초기화하지 못했습니다. email={}", email);
		}
	}

	private String normalizeEmail(final String email) {
		return (email == null) ? null : email.trim().toLowerCase();
	}

	private String buildLoginAttemptKey(final String email) {
		return "login:attempt:" + email;
	}

	private String buildLoginLockKey(final String email) {
		return "login:lock:" + email;
	}

	private String sha256(final String raw) {
		try {
			MessageDigest md = MessageDigest.getInstance("SHA-256");
			byte[] digest = md.digest(raw.getBytes(StandardCharsets.UTF_8));
			StringBuilder sb = new StringBuilder();
			for (byte b : digest) {
				sb.append(String.format("%02x", b));
			}
			return sb.toString();
		} catch (Exception e) {
			// fallback (매우 드문 케이스)
			return Long.toHexString(System.nanoTime());
		}
	}

}