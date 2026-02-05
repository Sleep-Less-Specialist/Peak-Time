package com.github.sleeplessspecialist.peaktime.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.service.UserService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * UserService의 비즈니스 로직(내 정보 조회, 수정)을 검증하는 단위 테스트 클래스입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@AfterEach
	void tearDown() {

		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("성공: 내 정보를 조회하면 UserProfileRes가 반환")
	void getMyProfile_Success() {
		// given
		Long userId = 1L;
		setupSecurityContext(userId);

		User user = User.createForSignup("테스터", "test@email.com", "pw", "01012345678");
		ReflectionTestUtils.setField(user, "id", userId);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserProfileRes result = userService.getMyProfile();

		// then
		assertThat(result.getId()).isEqualTo(userId);
		assertThat(result.getName()).isEqualTo("테스터");
		assertThat(result.getEmail()).isEqualTo("test@email.com");
	}

	@Test
	@DisplayName("실패: 존재하지 않는 유저(탈퇴 등) 조회 시 USER_NOT_FOUND 예외 발생")
	void getMyProfile_Fail_UserNotFound() {
		// given
		Long userId = 99L;
		setupSecurityContext(userId);

		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.getMyProfile())
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("성공: 내 정보를 수정하면 변경된 정보를 반영")
	void updateMyProfile_Success() {
		// given
		Long userId = 1L;
		setupSecurityContext(userId);

		User user = User.createForSignup("기존이름", "test@email.com", "pw", "01011112222");
		ReflectionTestUtils.setField(user, "id", userId);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		UserUpdateReq req = new UserUpdateReq();
		ReflectionTestUtils.setField(req, "name", "변경한이름");
		ReflectionTestUtils.setField(req, "phoneNumber", "01099998888");

		// when
		UserProfileRes result = userService.updateMyProfile(req);

		// then
		assertThat(result.getName()).isEqualTo("변경한이름");
		assertThat(result.getPhoneNumber()).isEqualTo("01099998888");

		assertThat(user.getName()).isEqualTo("변경한이름");
	}

	private void setupSecurityContext(Long userId) {
		Authentication authentication = mock(Authentication.class);

		when(authentication.getPrincipal()).thenReturn(userId);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);
	}
}