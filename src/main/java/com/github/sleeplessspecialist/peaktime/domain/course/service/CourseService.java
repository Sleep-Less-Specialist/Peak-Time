package com.github.sleeplessspecialist.peaktime.domain.course.service;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CurriculumDto;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.LecturerDto;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;

/**
 * 강의 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
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
		Course course = courseRepository.findById(courseId)
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

	/**
	 * 강의 전체 목록을 페이징하여 조회합니다.
	 *
	 * @param pageable 페이징 정보 (page, size, sort)
	 * @return 페이징된 강의 목록 DTO
	 */
	public Page<CourseListRes> getCourseList(Pageable pageable) {
		Page<Course> coursePage = courseRepository.findAll(pageable);

		return coursePage.map(course -> new CourseListRes(
			course.getId(),
			course.getTitle(),
			course.getDescription(),
			course.getCategory(),
			course.getPrice(),
			course.getThumbnailUrl(),
			course.getRatingAvg(),
			course.getReviewCount(),
			course.getLecturer().getName()
		));
	}
}