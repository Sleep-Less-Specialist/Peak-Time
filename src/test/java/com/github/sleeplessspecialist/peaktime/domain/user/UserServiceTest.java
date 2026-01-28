package com.github.sleeplessspecialist.peaktime.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;
import static org.mockito.Mockito.*;

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
 * UserServiceTest 클래스입니다.
 * <p>
 * UserService의 비즈니스 로직(내 정보 조회, 수정)을 검증하는 단위 테스트 클래스입니다.
 * Mockito를 사용하여 Repository와 SecurityContext를 모의(Mocking) 처리하여
 * 외부 의존성 없이 서비스 로직만 독립적으로 테스트합니다.
 * </p>
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
		// 테스트가 끝날 때마다 시큐리티 컨텍스트 초기화 (다른 테스트 간섭 방지)
		SecurityContextHolder.clearContext();
	}

	@Test
	@DisplayName("성공: 내 정보를 조회하면 UserProfileRes가 반환된다.")
	void getMyProfile_Success() {
		// given
		Long userId = 1L;
		setupSecurityContext(userId); // 1. 로그인된 상태 모의(Mocking)

		// 엔티티 생성 및 ID 주입 (Reflection 사용)
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
	@DisplayName("성공: 내 정보를 수정하면 변경된 정보가 반영된다.")
	void updateMyProfile_Success() {
		// given
		Long userId = 1L;
		setupSecurityContext(userId);

		// 기존 유저 정보
		User user = User.createForSignup("기존이름", "test@email.com", "pw", "01011112222");
		ReflectionTestUtils.setField(user, "id", userId);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// 수정 요청 DTO 생성
		UserUpdateReq req = new UserUpdateReq();
		ReflectionTestUtils.setField(req, "name", "변경한이름");
		ReflectionTestUtils.setField(req, "phoneNumber", "01099998888");

		// when
		UserProfileRes result = userService.updateMyProfile(req);

		// then
		assertThat(result.getName()).isEqualTo("변경한이름"); // 반환값 검증
		assertThat(result.getPhoneNumber()).isEqualTo("01099998888");

		// 실제 Entity 값도 변경되었는지 확인 (Dirty Checking 동작 검증)
		assertThat(user.getName()).isEqualTo("변경한이름");
	}

	// --- Helper Method ---

	/**
	 * SecurityUtil이 사용하는 SecurityContextHolder에 가짜 인증 정보를 넣습니다.
	 * SecurityUtil.getCurrentUserId() 호출 시 userId가 반환되도록 설정합니다.
	 */
	private void setupSecurityContext(Long userId) {
		Authentication authentication = mock(Authentication.class);
		// Principal을 Long 타입으로 반환하도록 설정 (SecurityUtil 구현에 맞춤)
		when(authentication.getPrincipal()).thenReturn(userId);

		SecurityContext securityContext = mock(SecurityContext.class);
		when(securityContext.getAuthentication()).thenReturn(authentication);

		SecurityContextHolder.setContext(securityContext);
	}
}