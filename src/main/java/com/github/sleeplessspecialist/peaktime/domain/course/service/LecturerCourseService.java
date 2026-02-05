package com.github.sleeplessspecialist.peaktime.domain.course.service;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseManagementDetailRes;
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

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class LecturerCourseService {

	private final CourseRepository courseRepository;
	private final UserRepository userRepository;

	/**
	 * 강의 등록
	 */
	@Transactional
	public CourseRegisterRes registerCourse(Long userId, CourseRegisterReq req) {

		User lecturer = userRepository.findById(userId)
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
	 * 강의 수정
	 */
	@Transactional
	public CourseUpdateRes updateCourse(Long userId, Long courseId, CourseUpdateReq req) {

		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

		if (!course.getLecturer().getId().equals(userId)) {
			throw new CustomException(CourseErrorCode.UNAUTHORIZED_ACCESS);
		}

		course.update(
			req.getTitle(),
			req.getDescription(),
			req.getCategory(),
			req.getPrice(),
			req.getThumbnailUrl()
		);

		return CourseUpdateRes.builder()
			.courseId(course.getId())
			.title(course.getTitle())
			.description(course.getDescription())
			.category(course.getCategory())
			.price(course.getPrice())
			.thumbnailUrl(course.getThumbnailUrl())
			.updatedAt(course.getUpdatedAt())
			.build();
	}

	/**
	 * 본인이 등록한 강의 목록 조회
	 */
	@Transactional(readOnly = true)
	public Page<CourseListRes> getMyCourses(Long userId, Pageable pageable) {

		Page<Course> coursePage = courseRepository.findAllByLecturerId(userId, pageable);

		return coursePage.map(course -> CourseListRes.builder()
			.courseId(course.getId())
			.title(course.getTitle())
			.description(course.getDescription())
			.category(course.getCategory())
			.price(course.getPrice())
			.thumbnailUrl(course.getThumbnailUrl())
			.ratingAvg(course.getRatingAvg())
			.reviewCount(course.getReviewCount())
			.lecturerName(course.getLecturer().getName())
			.build());
	}

	/**
	 * 강의 상세 조회
	 */
	@Transactional(readOnly = true)
	public CourseManagementDetailRes getCourseDetail(Long userId, Long courseId) {

		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(CourseErrorCode.COURSE_NOT_FOUND));

		if (!course.getLecturer().getId().equals(userId)) {
			throw new CustomException(CourseErrorCode.UNAUTHORIZED_ACCESS);
		}

		return CourseManagementDetailRes.builder()
			.courseId(course.getId())
			.title(course.getTitle())
			.description(course.getDescription())
			.category(course.getCategory())
			.price(course.getPrice())
			.thumbnailUrl(course.getThumbnailUrl())
			.ratingAvg(course.getRatingAvg())
			.reviewCount(course.getReviewCount())
			.lecturerName(course.getLecturer().getName())
			.createdAt(course.getCreatedAt())
			.updatedAt(course.getUpdatedAt())
			.build();
	}
}