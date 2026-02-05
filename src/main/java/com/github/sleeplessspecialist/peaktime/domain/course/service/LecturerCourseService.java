package com.github.sleeplessspecialist.peaktime.domain.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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
}