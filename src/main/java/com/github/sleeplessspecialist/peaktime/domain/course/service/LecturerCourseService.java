package com.github.sleeplessspecialist.peaktime.domain.course.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;

/**
 * LecturerCourseService 클래스입니다.
 * <p>
 * 지식공유자(Lecturer) 전용 강의 관리 서비스입니다.
 * 강의 등록, 수정, 삭제 등의 비즈니스 로직을 담당합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Service
@RequiredArgsConstructor
public class LecturerCourseService {

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
}
