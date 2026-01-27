package com.github.sleeplessspecialist.peaktime.domain.course.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CurriculumDto;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.LecturerDto;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;

/**
 * 강의 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 강의 등록, 조회, 수정 등의 실질적인 데이터 처리 흐름을 제어합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 22.
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class CourseService {

	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	/**
	 * 새로운 강의를 생성하고 저장합니다.
	 *
	 * @param req 강의 등록에 필요한 상세 정보
	 * @return 저장된 강의의 ID
	 * @throws CustomException 사용자를 찾을 수 없거나 권한이 없는 경우 예외 발생
	 */
	@Transactional
	public CourseRegisterRes registerCourse(CourseRegisterReq req) {
		// TODO: 추후 SecurityContextHolder를 통해 로그인한 유저 ID를 가져오도록 수정 필요
		Long mockUserId = 1L;

		User lecturer = userRepository.findById(mockUserId)
			.orElseThrow(() -> new CustomException(CourseErrorCode.USER_NOT_FOUND));

		Course course = Course.builder()
			.title(req.getTitle())
			.description(req.getDescription())
			.price(req.getPrice())
			.category(req.getCategory())
			.thumbnailUrl(req.getThumbnailUrl())
			.lecturer(lecturer)
			.build();

		Course savedCourse = courseRepository.save(course);

		return new CourseRegisterRes(savedCourse.getId());
	}

	/**
	 * 강의 상세 정보를 조회합니다.
	 * <p>
	 * 강의 기본 정보뿐만 아니라 지식공유자(Lecturer) 정보와
	 * 커리큘럼(Lecture List)을 함께 반환합니다.
	 * </p>
	 *
	 * @param courseId 조회할 강의 ID
	 * @return 강의 상세 응답 DTO (Curriculum 포함)
	 */
	public CourseDetailRes getCourseDetail(Long courseId) {

		Course course = courseRepository.findByIdWithDetail(courseId)
			.orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

		LecturerDto lecturerDto = new LecturerDto(
			course.getLecturer().getId(),
			course.getLecturer().getName()
		);

		List<CurriculumDto> curriculumList = course.getLectures().stream()
			.map(lecture -> new CurriculumDto(
				lecture.getId(),
				lecture.getTitle(),
				lecture.getDuration()
			))
			.collect(Collectors.toList());

		return new CourseDetailRes(
			course.getId(),
			course.getTitle(),
			course.getDescription(),
			course.getPrice(),
			course.getThumbnailUrl(),
			course.getRatingAvg(),
			course.getReviewCount(),
			lecturerDto,
			curriculumList,
			course.getUpdatedAt()
		);
	}
}