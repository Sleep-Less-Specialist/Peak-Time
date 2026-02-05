package com.github.sleeplessspecialist.peaktime.domain.user;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.EnrollmentStatus;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.MyCourseRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.service.UserService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * UserService의 비즈니스 로직(내 정보 조회, 수정, 강의 목록 조회)을 검증하는 단위 테스트 클래스입니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 28.
 */
@ExtendWith(MockitoExtension.class)
class UserServiceTest {

	@InjectMocks
	private UserService userService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private EnrollmentRepository enrollmentRepository;

	@Test
	@DisplayName("성공: 내 정보를 조회하면 UserProfileRes가 반환된다.")
	void getMyProfile_Success() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("테스터", "test@email.com", "pw", "01012345678");
		ReflectionTestUtils.setField(user, "id", userId);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		// when
		UserProfileRes result = userService.getMyProfile(userId);

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
		given(userRepository.findById(userId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> userService.getMyProfile(userId))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(UserErrorCode.USER_NOT_FOUND);
	}

	@Test
	@DisplayName("성공: 내 정보를 수정하면 변경된 정보를 반영하여 반환한다.")
	void updateMyProfile_Success() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("기존이름", "test@email.com", "pw", "01011112222");
		ReflectionTestUtils.setField(user, "id", userId);

		given(userRepository.findById(userId)).willReturn(Optional.of(user));

		UserUpdateReq req = new UserUpdateReq();
		ReflectionTestUtils.setField(req, "name", "변경한이름");
		ReflectionTestUtils.setField(req, "phoneNumber", "01099998888");

		// when
		UserProfileRes result = userService.updateMyProfile(userId, req);

		// then
		assertThat(result.getName()).isEqualTo("변경한이름");
		assertThat(result.getPhoneNumber()).isEqualTo("01099998888");
		assertThat(user.getName()).isEqualTo("변경한이름");
	}

	@Test
	@DisplayName("성공: 내 강의 목록을 조회하면 Enrollment 정보를 MyCourseRes 리스트로 변환하여 반환한다.")
	void getMyCourses_Success() {
		// given
		Long userId = 1L;
		given(userRepository.existsById(userId)).willReturn(true);
		User lecturer = User.createForSignup("김강사", "tutor@test.com", "pw", "01012345678");
		ReflectionTestUtils.setField(lecturer, "id", 10L);

		Course course = Course.builder()
			.title("스프링부트 정복")
			.price(BigDecimal.valueOf(50000))
			.thumbnailUrl("thumb.jpg")
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course, "id", 100L);

		Enrollment enrollment = Enrollment.builder()
			.course(course)
			.user(mock(User.class))
			.build();
		ReflectionTestUtils.setField(enrollment, "createdAt", LocalDateTime.now());

		// Mocking
		given(enrollmentRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, EnrollmentStatus.ENROLLED))
			.willReturn(List.of(enrollment));

		// when
		List<MyCourseRes> result = userService.getMyCourses(userId);

		// then
		assertThat(result).hasSize(1);

		assertThat(result.get(0).title()).isEqualTo("스프링부트 정복");
		assertThat(result.get(0).lecturerName()).isEqualTo("김강사");
		assertThat(result.get(0).price()).isEqualTo(BigDecimal.valueOf(50000));

		verify(enrollmentRepository).findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, EnrollmentStatus.ENROLLED);
	}

	@Test
	@DisplayName("성공: 수강 중인 강의가 없으면 빈 리스트를 반환한다.")
	void getMyCourses_Success_Empty() {
		// given
		Long userId = 1L;
		given(userRepository.existsById(userId)).willReturn(true);
		given(enrollmentRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(userId, EnrollmentStatus.ENROLLED))
			.willReturn(Collections.emptyList());

		// when
		List<MyCourseRes> result = userService.getMyCourses(userId);

		// then
		assertThat(result).isEmpty();
	}

	@Test
	@DisplayName("실패: 강의 목록 조회 시 존재하지 않는 유저라면 예외(USER_NOT_FOUND)가 발생한다.")
	void getMyCourses_Fail_UserNotFound() {
		// given
		Long userId = 99L;

		given(userRepository.existsById(userId)).willReturn(false);

		// when & then
		assertThatThrownBy(() -> userService.getMyCourses(userId))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(UserErrorCode.USER_NOT_FOUND);

		verify(userRepository).existsById(userId);
		verify(enrollmentRepository, never()).findAllByUserIdAndStatusOrderByCreatedAtDesc(any(), any());
	}
}