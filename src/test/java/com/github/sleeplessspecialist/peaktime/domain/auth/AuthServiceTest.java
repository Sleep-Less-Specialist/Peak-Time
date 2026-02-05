package com.github.sleeplessspecialist.peaktime.domain.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.Duration;
import java.util.Optional;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LoginReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.LoginRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserRole;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserStatus;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtProperties;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenProvider;

/**
 * AuthServiceTest 테스트 클래스입니다.
 * <p>
 * AuthService의 회원가입/로그인 등 비즈니스 로직을 검증하는 테스트 클래스입니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 25.
 */
@ExtendWith(MockitoExtension.class)
public class AuthServiceTest {

	@InjectMocks
	private AuthService authService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private PointService pointService;

	@Mock
	private PasswordEncoder passwordEncoder;

	@Mock
	private JwtTokenProvider jwtTokenProvider;

	@Mock
	private StringRedisTemplate stringRedisTemplate;

	@Mock
	private JwtProperties jwtProperties;

	@Mock
	private ValueOperations<String, String> valueOperations;

	/**
	 * 회원가입 성공 시 내부 로직 테스트
	 */
	@Test
	@DisplayName("회원가입 성공 시 로직 검증")
	void signup_success() {
		// given
		SignupReq request = new SignupReq(
			"test@email.com",
			"Aa!12345678",
			"재원",
			"01012345678"
		);

		when(userRepository.existsByEmail(request.getEmail()))
			.thenReturn(false);

		when(passwordEncoder.encode(anyString()))
			.thenReturn("encodedPassword");

		User savedUser = User.createForSignup(
			request.getName(),
			request.getEmail(),
			"encodedPassword",
			request.getPhoneNumber()
		);
		assertThat(savedUser.getEmail()).isEqualTo(request.getEmail());

		when(userRepository.save(any(User.class)))
			.thenReturn(savedUser);

		// when
		SignupRes result = authService.signup(request);

		// then
		assertThat(result.getEmail()).isEqualTo(request.getEmail());
		assertThat(result.getName()).isEqualTo(request.getName());

		verify(userRepository).save(any(User.class));
		verify(pointService).grantSignupBonus(savedUser);
	}

	/**
	 * 로그인 성공 시 내부 로직 테스트
	 */
	@Test
	@DisplayName("로그인 성공 시 토큰 발급 및 Redis 저장/초기화 수행")
	void login_success() {
		// given
		LoginReq request = new LoginReq("User@Example.com", "P@ssw0rd!234");
		String normalizedEmail = "user@example.com";

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		// 브루트포스 잠금 없음
		when(stringRedisTemplate.hasKey("login:lock:" + normalizedEmail)).thenReturn(false);

		User user = mock(User.class);
		when(userRepository.findByEmail(normalizedEmail)).thenReturn(Optional.of(user));
		when(user.getStatus()).thenReturn(UserStatus.ACTIVE);
		when(user.getPasswordHash()).thenReturn("hashed");
		when(user.getId()).thenReturn(1L);
		when(user.getRole()).thenReturn(UserRole.STUDENT);

		when(passwordEncoder.matches("P@ssw0rd!234", "hashed")).thenReturn(true);

		when(jwtTokenProvider.createAccessToken(1L, "STUDENT")).thenReturn("access-token");
		when(jwtTokenProvider.createRefreshToken(1L)).thenReturn("refresh-token");

		when(jwtProperties.getRefreshTokenExpirationMs()).thenReturn(600_000L);
		when(jwtProperties.getAccessTokenExpirationMs()).thenReturn(1_800_000L);

		// when
		LoginRes result = authService.login(request, "test-user-agent");

		// then
		assertThat(result.getAccessToken()).isEqualTo("access-token");
		assertThat(result.getRefreshToken()).isEqualTo("refresh-token");
		assertThat(result.getTokenType()).isEqualTo("Bearer");
		assertThat(result.getExpiresIn()).isEqualTo(1800);

		// RefreshToken 화이트리스트 저장(TTL 포함)
		verify(valueOperations).set(
			eq("refresh:1:refresh-token"),
			eq("1"),
			eq(Duration.ofMillis(600_000L))
		);

		// 로그인 성공 시 시도 횟수 초기화
		verify(stringRedisTemplate).delete("login:attempt:" + normalizedEmail);
		verify(stringRedisTemplate).delete("login:lock:" + normalizedEmail);
	}

	@Test
	@DisplayName("이메일이 존재하지 않으면 401 반환 및 실패 카운트가 증가한다")
	void login_invalidCredentials_incrementsAttempt_andReturns401() {
		// given
		LoginReq request = new LoginReq("user@example.com", "wrong");
		String email = "user@example.com";

		when(stringRedisTemplate.hasKey("login:lock:" + email)).thenReturn(false);
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		when(valueOperations.increment("login:attempt:" + email)).thenReturn(1L);

		// when
		CustomException ex = Assertions.<CustomException>assertThrows(
			CustomException.class,
			() -> authService.login(request, "test-user-agent")
		);

		// then
		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.INVALID_CREDENTIALS);
		verify(valueOperations).increment("login:attempt:" + email);
		verify(stringRedisTemplate).expire("login:attempt:" + email, Duration.ofMinutes(10));
	}

	@Test
	@DisplayName("로그인 실패가 임계치에 도달하면 429(TOO_MANY_ATTEMPTS)로 전환된다")
	void login_onFifthFailure_returns429() {
		// given
		LoginReq request = new LoginReq("user@example.com", "wrong");
		String email = "user@example.com";

		when(stringRedisTemplate.hasKey("login:lock:" + email)).thenReturn(false);
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		when(stringRedisTemplate.opsForValue()).thenReturn(valueOperations);

		// 이미 5회째 실패
		when(valueOperations.increment("login:attempt:" + email)).thenReturn(5L);
		when(valueOperations.setIfAbsent(
			eq("login:lock:" + email),
			eq("1"),
			eq(Duration.ofMinutes(10))
		)).thenReturn(true);

		// when
		CustomException ex = Assertions.<CustomException>assertThrows(
			CustomException.class,
			() -> authService.login(request, "test-user-agent")
		);

		// then
		assertThat(ex.getErrorCode()).isEqualTo(AuthErrorCode.TOO_MANY_ATTEMPTS);
	}
}
