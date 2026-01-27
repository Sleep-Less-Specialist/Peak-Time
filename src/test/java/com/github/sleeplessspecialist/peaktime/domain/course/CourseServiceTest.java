package com.github.sleeplessspecialist.peaktime.domain.course;

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
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;
import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * CourseService 비즈니스 로직 테스트 클래스입니다.
 * <p>
 * 대상 클래스: CourseService
 * 주요 기능: 강의 등록, 강의 상세 조회
 * </p>
 *
 * @author 기섭
 * @since 2026. 1. 27.
 */
@ExtendWith(MockitoExtension.class) // Mockito 환경 사용
class CourseServiceTest {

	@InjectMocks
	private CourseService courseService;

	@Mock
	private CourseRepository courseRepository;

	@Test
	@DisplayName("성공: 강의 ID로 상세 정보를 조회하면, CourseDetailRes DTO가 반환된다.")
	void getCourseDetail_Success() {
		// given
		// 1. 강사(Lecturer) 생성 (Reflection으로 ID 주입)
		User lecturer = User.createForSignup("김스프링", "test@test.com", "hash", "01012345678");
		ReflectionTestUtils.setField(lecturer, "id", 1L);

		// 2. 강의(Course) 생성
		Course course = Course.builder()
			.title("스프링 완전 정복")
			.description("설명")
			.category("BACKEND")
			.price(BigDecimal.valueOf(50000))
			.thumbnailUrl("thumb.jpg")
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course, "id", 100L);

		// 3. 커리큘럼(Lecture) 생성 및 추가
		Lecture lecture1 = Lecture.builder().title("1강 OT").duration(600).course(course).build();
		Lecture lecture2 = Lecture.builder().title("2강 개요").duration(1200).course(course).build();

		// Entity의 lectures 리스트에 수동으로 추가 (테스트 환경이므로)
		course.getLectures().add(lecture1);
		course.getLectures().add(lecture2);

		// 4. Mocking: Repository 호출 시 위에서 만든 course 반환
		given(courseRepository.findByIdWithDetail(100L)).willReturn(Optional.of(course));

		// when
		CourseDetailRes result = courseService.getCourseDetail(100L);

		// then
		// 1. 강의 기본 정보 검증
		assertThat(result.getCourseId()).isEqualTo(100L);
		assertThat(result.getTitle()).isEqualTo("스프링 완전 정복");

		// 2. 지식공유자(Lecturer) 정보 검증 (Tutor -> Lecturer 용어 변경 반영 확인)
		assertThat(result.getLecturer().getLecturerId()).isEqualTo(1L);
		assertThat(result.getLecturer().getName()).isEqualTo("김스프링");

		// 3. 커리큘럼(Lecture) 정보 검증
		assertThat(result.getCurriculum()).hasSize(2);
		assertThat(result.getCurriculum().get(0).getTitle()).isEqualTo("1강 OT");
		assertThat(result.getCurriculum().get(0).getDuration()).isEqualTo(600);

		// 4. 호출 여부 검증
		verify(courseRepository).findByIdWithDetail(100L);
	}

	@Test
	@DisplayName("실패: 존재하지 않는 강의 ID 조회 시 COURSE_NOT_FOUND 예외가 발생한다.")
	void getCourseDetail_Fail_NotFound() {
		// given
		Long invalidCourseId = 999L;
		given(courseRepository.findByIdWithDetail(invalidCourseId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> courseService.getCourseDetail(invalidCourseId))
			.isInstanceOf(CustomException.class)
			.extracting("errorCode")
			.isEqualTo(CourseErrorCode.COURSE_NOT_FOUND);
	}

	@Test
	@DisplayName("성공: 강의 목록을 페이징하여 조회하면, CourseListRes Page가 반환된다.")
	void getCourseList_Success() {
		// given
		// 1. 페이징 요청 객체 생성 (0페이지, 10개씩)
		Pageable pageable = PageRequest.of(0, 10);

		// 2. 강사 및 강의 데이터 생성
		User lecturer = User.createForSignup("이자바", "java@test.com", "hash", "01011112222");

		Course course1 = Course.builder()
			.title("자바의 정석")
			.description("기초")
			.category("BACKEND")
			.price(BigDecimal.valueOf(10000))
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course1, "id", 1L);

		Course course2 = Course.builder()
			.title("JPA 프로그래밍")
			.description("심화")
			.category("BACKEND")
			.price(BigDecimal.valueOf(20000))
			.lecturer(lecturer)
			.build();
		ReflectionTestUtils.setField(course2, "id", 2L);

		// 3. Mock Page 객체 생성 (DB에서 반환될 예상 결과)
		List<Course> courseList = List.of(course1, course2);
		Page<Course> mockPage = new PageImpl<>(courseList, pageable, 2);

		// 4. Mocking: Repository 호출 시 mockPage 반환
		given(courseRepository.findAllWithLecturer(any(Pageable.class))).willReturn(mockPage);

		// when
		Page<CourseListRes> result = courseService.getCourseList(pageable);

		// then
		// 1. 페이지 크기 및 내용 검증
		assertThat(result.getContent()).hasSize(2);
		assertThat(result.getTotalElements()).isEqualTo(2);

		// 2. 데이터 매핑 검증 (Entity -> DTO 변환 확인)
		assertThat(result.getContent().get(0).getTitle()).isEqualTo("자바의 정석");
		assertThat(result.getContent().get(0).getLecturerName()).isEqualTo("이자바"); // 강사 이름 확인
		assertThat(result.getContent().get(1).getPrice()).isEqualTo(BigDecimal.valueOf(20000));

		// 3. 호출 검증
		verify(courseRepository).findAllWithLecturer(any(Pageable.class));
	}
}