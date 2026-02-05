package com.github.sleeplessspecialist.peaktime.domain.course.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseUpdateRes;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * LecturerCourseService(지식공유자 전용) 비즈니스 로직 테스트 클래스
 * <p>
 * 대상: LecturerCourseService
 * 기능: 강의 등록, 수정
 * </p>
 *
 * @author 기섭
 * @since 2026. 1. 28.
 */
@ExtendWith(MockitoExtension.class)
class LecturerCourseServiceTest {

	@InjectMocks
	private LecturerCourseService lecturerCourseService;

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private UserRepository userRepository;

	@Test
	@DisplayName("registerCourse: 강의 등록 정보를 입력하면 저장 후 생성된 강의 ID를 반환")
	void registerCourse_Success() {
		// given
		Long userId = 1L;

		CourseRegisterReq req = new CourseRegisterReq();
		ReflectionTestUtils.setField(req, "title", "새로운 강의");
		ReflectionTestUtils.setField(req, "description", "강의 설명입니다.");
		ReflectionTestUtils.setField(req, "category", "BACKEND");
		ReflectionTestUtils.setField(req, "price", BigDecimal.valueOf(30000));
		ReflectionTestUtils.setField(req, "thumbnailUrl", "https://thumb.jpg");

		User lecturer = User.createForSignup("김강사", "tutor@test.com", "hash", "01099998888");
		ReflectionTestUtils.setField(lecturer, "id", userId);

		Course savedCourse = Course.builder()
			.title(req.getTitle())
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(savedCourse, "id", 100L);

		given(userRepository.findById(userId)).willReturn(Optional.of(lecturer));
		given(courseRepository.save(any(Course.class))).willReturn(savedCourse);

		// when
		CourseRegisterRes result = lecturerCourseService.registerCourse(userId, req);

		// then
		assertThat(result.getCourseId()).isEqualTo(100L);

		verify(userRepository).findById(userId);
		verify(courseRepository).save(any(Course.class));
	}

	@Test
	@DisplayName("updateCourse: 본인 강의를 수정하면 값이 변경되고 결과를 반환")
	void updateCourse_Success() {
		// given
		Long userId = 1L;
		Long courseId = 100L;

		CourseUpdateReq req = CourseUpdateReq.builder()
			.title("수정된 제목")
			.description("수정된 설명")
			.category("FRONTEND")
			.price(BigDecimal.valueOf(50000))
			.thumbnailUrl("https://new-thumb.jpg")
			.build();

		User lecturer = User.createForSignup("김강사", "test@test.com", "pw", "01012345678");
		ReflectionTestUtils.setField(lecturer, "id", userId);

		Course course = Course.builder()
			.title("옛날 제목")
			.description("옛날 설명")
			.price(BigDecimal.valueOf(10000))
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course, "id", courseId);

		// Mocking
		given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

		// when
		CourseUpdateRes result = lecturerCourseService.updateCourse(userId, courseId, req);

		// then
		assertThat(result.getTitle()).isEqualTo("수정된 제목");
		assertThat(result.getPrice()).isEqualTo(BigDecimal.valueOf(50000));

		assertThat(course.getTitle()).isEqualTo("수정된 제목");
		assertThat(course.getDescription()).isEqualTo("수정된 설명");
		assertThat(course.getCategory()).isEqualTo("FRONTEND");
	}

	@Test
	@DisplayName("updateCourse: 본인 강의가 아니면 권한 예외(UNAUTHORIZED_ACCESS)가 발생한다.")
	void updateCourse_Fail_Unauthorized() {
		// given
		Long attackerId = 999L;
		Long courseId = 100L;
		Long ownerId = 1L;

		CourseUpdateReq req = new CourseUpdateReq();

		User owner = User.createForSignup("주인", "owner@test.com", "pw", "01000000000");
		ReflectionTestUtils.setField(owner, "id", ownerId);

		Course course = Course.builder().lecturer(owner).build();

		given(courseRepository.findById(courseId)).willReturn(Optional.of(course));

		// when & then
		assertThatThrownBy(() -> lecturerCourseService.updateCourse(attackerId, courseId, req))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(CourseErrorCode.UNAUTHORIZED_ACCESS);
	}

	@Test
	@DisplayName("updateCourse: 존재하지 않는 강의 ID를 수정하려 하면 예외가 발생한다.")
	void updateCourse_Fail_NotFound() {
		// given
		Long userId = 1L;
		Long weirdId = 9999L;
		CourseUpdateReq req = new CourseUpdateReq();

		given(courseRepository.findById(weirdId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> lecturerCourseService.updateCourse(userId, weirdId, req))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(CourseErrorCode.COURSE_NOT_FOUND);
	}

	@Test
	@DisplayName("getMyCourses: 본인이 등록한 강의 목록을 페이징하여 조회한다.")
	void getMyCourses_Success() {
		// given
		Long userId = 1L;
		Pageable pageable = PageRequest.of(0, 10);

		User lecturer = User.createForSignup("나강사", "tutor@test.com", "pw", "01011112222");
		ReflectionTestUtils.setField(lecturer, "id", userId);
		ReflectionTestUtils.setField(lecturer, "name", "나강사");

		Course course = Course.builder()
			.title("테스트 강의")
			.description("강의 설명")
			.price(BigDecimal.valueOf(10000))
			.category("IT")
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course, "id", 100L);

		List<Course> courseList = List.of(course);
		Page<Course> coursePage = new PageImpl<>(courseList, pageable, 1);

		given(courseRepository.findAllByLecturerId(userId, pageable)).willReturn(coursePage);

		// when
		Page<CourseListRes> result = lecturerCourseService.getMyCourses(userId, pageable);

		// then
		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("테스트 강의");
		assertThat(result.getContent().get(0).getLecturerName()).isEqualTo("나강사");

		verify(courseRepository).findAllByLecturerId(userId, pageable);
	}
}