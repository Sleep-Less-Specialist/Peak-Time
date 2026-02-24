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

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * CourseService(조회 전용) 비즈니스 로직 테스트 클래스입니다.
 * <p>
 * 대상 클래스: CourseService
 * 주요 기능: 강의 상세 조회, 강의 목록 조회
 * </p>
 *
 * @author 기섭
 * @since 2026. 1. 27.
 */
@ExtendWith(MockitoExtension.class)
class CourseServiceTest {

	@InjectMocks
	private CourseService courseService;

	@Mock
	private CourseRepository courseRepository;

	@Test
	@DisplayName("성공: 강의 ID로 상세 정보를 조회하면, CourseDetailRes DTO가 반환된다.")
	void getCourseDetail_Success() {

		// given
		User lecturer = User.createForSignup("김스프링", "test@test.com", "hash", "01012345678");
		ReflectionTestUtils.setField(lecturer, "id", 1L);

		Course course = Course.builder()
			.title("스프링 완전 정복")
			.description("설명")
			.category("BACKEND")
			.price(BigDecimal.valueOf(50000))
			.thumbnailUrl("thumb.jpg")
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course, "id", 100L);

		Lecture lecture1 = Lecture.builder().title("1강 OT").duration(600).course(course).build();
		Lecture lecture2 = Lecture.builder().title("2강 개요").duration(1200).course(course).build();
		course.getLectures().add(lecture1);
		course.getLectures().add(lecture2);

		given(courseRepository.findById(100L)).willReturn(Optional.of(course));

		// when
		CourseDetailRes result = courseService.getCourseDetail(100L);

		// then
		assertThat(result.getCourseId()).isEqualTo(100L);
		assertThat(result.getLecturer().getName()).isEqualTo("김스프링");
		assertThat(result.getCurriculum()).hasSize(2);

		verify(courseRepository).findById(100L);
	}

	@Test
	@DisplayName("실패: 존재하지 않는 강의 ID 조회 시 COURSE_NOT_FOUND 예외가 발생한다.")
	void getCourseDetail_Fail_NotFound() {

		// given
		Long invalidCourseId = 999L;
		given(courseRepository.findById(invalidCourseId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> courseService.getCourseDetail(invalidCourseId))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(CourseErrorCode.COURSE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공: 검색 조건과 함께 강의 목록을 페이징 조회하면, CourseListRes Page가 반환된다.")
	void getCourseList_Success() {

		// given
		Pageable pageable = PageRequest.of(0, 10);

		CourseSearchCondition condition = new CourseSearchCondition(
			null,
			null,
			null,
			null,
			null,
			null
		);

		User lecturer = User.createForSignup("이자바", "java@test.com", "hash", "01011112222");
		ReflectionTestUtils.setField(lecturer, "id", 10L);

		Course course1 = Course.builder()
			.title("자바")
			.description("설명")
			.category("BACKEND")
			.price(BigDecimal.valueOf(10000))
			.thumbnailUrl(null)
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course1, "id", 1L);

		Course course2 = Course.builder()
			.title("JPA")
			.description("설명")
			.category("BACKEND")
			.price(BigDecimal.valueOf(20000))
			.thumbnailUrl(null)
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course2, "id", 2L);

		List<Course> courseList = List.of(course1, course2);
		Page<Course> mockPage = new PageImpl<>(courseList, pageable, 2);

		given(courseRepository.searchCourses(any(CourseSearchCondition.class), any(Pageable.class)))
			.willReturn(mockPage);

		// when
		Page<CourseListRes> result = courseService.getCourseList(condition, pageable);

		// then
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getContent().get(0).lecturerName()).isEqualTo("이자바");

		verify(courseRepository).searchCourses(any(CourseSearchCondition.class), any(Pageable.class));
	}

	@Test
	@DisplayName("성공: 검색 조건이 repository로 정상 전달된다.")
	void getCourseList_WithCondition_PassesConditionCorrectly() {

		// given
		Pageable pageable = PageRequest.of(0, 5);

		CourseSearchCondition condition = new CourseSearchCondition(
			"BACKEND",
			"spring",
			BigDecimal.valueOf(10000),
			BigDecimal.valueOf(50000),
			CourseSearchCondition.CourseSortBy.PRICE,
			CourseSearchCondition.SortDirection.ASC
		);

		Page<Course> mockPage = Page.empty(pageable);
		given(courseRepository.searchCourses(any(CourseSearchCondition.class), any(Pageable.class)))
			.willReturn(mockPage);

		// when
		courseService.getCourseList(condition, pageable);

		// then
		var conditionCaptor = org.mockito.ArgumentCaptor.forClass(CourseSearchCondition.class);

		verify(courseRepository).searchCourses(conditionCaptor.capture(), eq(pageable));

		CourseSearchCondition captured = conditionCaptor.getValue();

		assertThat(captured.category()).isEqualTo("BACKEND");
		assertThat(captured.keyword()).isEqualTo("spring");
		assertThat(captured.minPrice()).isEqualByComparingTo("10000");
		assertThat(captured.maxPrice()).isEqualByComparingTo("50000");
		assertThat(captured.sortBy()).isEqualTo(CourseSearchCondition.CourseSortBy.PRICE);
		assertThat(captured.direction()).isEqualTo(CourseSearchCondition.SortDirection.ASC);
	}
}