package com.github.sleeplessspecialist.peaktime.domain.auth;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;

/**
 * AuthServiceTest 테스트 클래스입니다.
 * <p>
 * 대상 클래스(또는 메서드): TODO 작성
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
}
