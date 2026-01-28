package com.github.sleeplessspecialist.peaktime.domain.course.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.math.BigDecimal;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;

/**
 * LecturerCourseService(지식공유자 전용) 비즈니스 로직 테스트 클래스입니다.
 * <p>
 * 대상 클래스: LecturerCourseService
 * 주요 기능: 강의 등록, 수정, 삭제
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
	@DisplayName("성공: 강의 등록 정보를 입력하면 저장 후 생성된 강의 ID를 반환한다.")
	void registerCourse_Success() {

		// given
		CourseRegisterReq req = new CourseRegisterReq();
		ReflectionTestUtils.setField(req, "title", "새로운 강의");
		ReflectionTestUtils.setField(req, "description", "강의 설명입니다.");
		ReflectionTestUtils.setField(req, "category", "BACKEND");
		ReflectionTestUtils.setField(req, "price", BigDecimal.valueOf(30000));
		ReflectionTestUtils.setField(req, "thumbnailUrl", "https://thumb.jpg");

		User lecturer = User.createForSignup("김강사", "tutor@test.com", "hash", "01099998888");
		ReflectionTestUtils.setField(lecturer, "id", 1L);

		Course savedCourse = Course.builder()
			.title(req.getTitle())
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(savedCourse, "id", 100L); // DB 저장을 흉내내어 ID 주입

		given(userRepository.findById(1L)).willReturn(Optional.of(lecturer)); // 유저 찾기 성공
		given(courseRepository.save(any(Course.class))).willReturn(savedCourse); // 저장 성공

		// when
		CourseRegisterRes result = lecturerCourseService.registerCourse(req);

		// then
		assertThat(result.getCourseId()).isEqualTo(100L);
		
		verify(userRepository).findById(1L);
		verify(courseRepository).save(any(Course.class));
	}
}