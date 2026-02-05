package com.github.sleeplessspecialist.peaktime.global.common.security.handler;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.time.Duration;
import java.time.Instant;
import java.util.Arrays;
import java.util.Map;
import java.util.Optional;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.security.core.Authentication;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.token.RefreshTokenStore;
import com.github.sleeplessspecialist.peaktime.domain.auth.token.SessionEntry;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtProperties;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenProvider;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 로그인 인증 성공 이후의 후처리를 담당하는 핸들러입니다.
 *
 * <p>
 * Spring Security OAuth2 로그인 플로우에서 외부 인증 제공자(provider)를 통한
 * 인증이 성공하면 호출되며, 인증 성공 이후의 애플리케이션 동작을 정의합니다.
 * </p>
 *
 * <h3>인증 정책</h3>
 * <ul>
 *   <li>OAuth2 인증 성공 시 이메일을 사용자 식별자로 사용합니다.</li>
 *   <li>동일 이메일의 기존 계정이 존재하는 경우, 자동 연동 정책에 따라 기존 사용자 계정으로 로그인 처리합니다.</li>
 *   <li>OAuth 제공자로부터 전달되는 이메일은 소유가 검증된 값이라는 전제를 둡니다.</li>
 * </ul>
 *
 * <p>
 * 이후 단계에서 수행되는 주요 작업은 다음과 같습니다.
 * </p>
 * <ul>
 *   <li>AccessToken / RefreshToken(JWT) 발급</li>
 *   <li>RefreshToken 해시값 Redis 화이트리스트 저장 (userId + deviceId)</li>
 *   <li>로그인 성공 후 메인 페이지 리다이렉트</li>
 * </ul>
 *
 * <p>
 * 추후 보안 정책 강화 시,
 * 자동 연동 이전에 추가 인증(step-up) 절차를 도입할 수 있도록 확장 가능합니다.
 * </p>
 *
 * @author 재원
 * @version 1.2
 * @since 2026. 1. 30.
 */
@Component
@RequiredArgsConstructor
@Slf4j
public class OAuth2SuccessHandler
	implements AuthenticationSuccessHandler {

	private final UserRepository userRepository;
	private final JwtTokenProvider jwtTokenProvider;
	private final JwtProperties jwtProperties;
	private final RefreshTokenStore refreshTokenStore;

	/**
	 * OAuth2 인증 제공자를 통한 로그인 인증이 성공했을 때 호출됩니다.
	 * <p>
	 * 이후 단계에서 사용자 유형(기존/신규)에 따른 분기 처리, JWT 발급 및 전달 전략(JSON 응답 또는 Redirect)이 이 메서드에 추가될 예정입니다.
	 * </p>
	 *
	 * @param request        OAuth2 로그인 성공 요청 정보
	 * @param response       OAuth2 로그인 성공 응답 객체
	 * @param authentication OAuth2 인증 완료 후 생성된 인증 정보
	 * @throws IOException 응답 처리 중 오류 발생 시
	 */
	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException {

		log.info("OAuth2 로그인 성공 - 토큰 발급 및 리다이렉트 처리를 시작합니다.");

		OAuth2User oAuth2User = (OAuth2User)authentication.getPrincipal();

		String email = extractEmail(oAuth2User);
		if (email == null || email.isBlank()) {
			log.warn("OAuth2 로그인은 성공했으나 이메일 정보가 전달되지 않았습니다. attributes={}", oAuth2User.getAttributes());
			response.setStatus(HttpServletResponse.SC_FOUND);
			response.setHeader(HttpHeaders.LOCATION, "/index.html?login=failed");
			return;
		}

		User user = userRepository.findByEmail(email)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		String deviceId = resolveDeviceId(request);
		setDeviceIdCookie(response, deviceId);

		String accessToken = jwtTokenProvider.createAccessToken(user.getId(), user.getRole().name());
		String refreshToken = jwtTokenProvider.createRefreshToken(user.getId());

		Instant now = Instant.now();

		String refreshTokenHash = sha256(refreshToken);

		Duration ttl = Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs());
		refreshTokenStore.save(
			new SessionEntry(
				user.getId(),
				deviceId,
				refreshTokenHash,
				now,
				ttl
			)
		);

		setRefreshTokenCookie(response, refreshToken, Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()));

		// 온보딩 분기: 전화번호(필수 정책)가 없다면 온보딩으로 유도 - 결제 시 환불 등의 정책을 준수하기 위해
		boolean needOnboarding = isBlank(user.getPhoneNumber());

		String redirectUrl = needOnboarding
			? "/index.html?login=success&onboarding=true"
			: "/index.html?login=success";

		response.setStatus(HttpServletResponse.SC_FOUND);
		response.setHeader(HttpHeaders.LOCATION, redirectUrl);
	}

	private static boolean isBlank(String value) {
		return value == null || value.isBlank();
	}

	@SuppressWarnings("unchecked")
	private String extractEmail(OAuth2User oAuth2User) {
		Object direct = oAuth2User.getAttributes().get("email");
		if (direct instanceof String s && !s.isBlank()) {
			return s;
		}

		// Kakao: { kakao_account: { email: ... } }
		Object kakaoAccount = oAuth2User.getAttributes().get("kakao_account");
		if (kakaoAccount instanceof Map<?, ?> map) {
			Object email = map.get("email");
			if (email instanceof String s && !s.isBlank()) {
				return s;
			}
		}

		return null;
	}

	private String resolveDeviceId(HttpServletRequest request) {
		return Optional.ofNullable(request.getCookies())
			.flatMap(cookies -> Arrays.stream(cookies)
				.filter(c -> "deviceId".equals(c.getName()))
				.map(c -> c.getValue())
				.filter(v -> v != null && !v.isBlank())
				.findFirst()
			)
			.orElseGet(() -> {
				String ua = Optional.ofNullable(request.getHeader("User-Agent")).orElse("unknown");
				String raw = ua + ":" + Instant.now().toEpochMilli();
				return sha256(raw).substring(0, 32);
			});
	}

	private void setDeviceIdCookie(HttpServletResponse response, String deviceId) {
		ResponseCookie cookie = ResponseCookie.from("deviceId", deviceId)
			.path("/")
			.httpOnly(false)
			.secure(false) // 로컬 개발 기준. 운영에서는 true(HTTPS)
			.sameSite("Lax")
			.maxAge(Duration.ofDays(30))
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private void setRefreshTokenCookie(HttpServletResponse response, String refreshToken, Duration maxAge) {
		ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
			.path("/")
			.httpOnly(true)
			.secure(false) // 로컬 개발 기준. 운영에서는 true(HTTPS)
			.sameSite("Lax")
			.maxAge(maxAge)
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

	private String sha256(String raw) {
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