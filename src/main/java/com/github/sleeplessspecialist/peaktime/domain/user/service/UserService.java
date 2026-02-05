package com.github.sleeplessspecialist.peaktime.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.EnrollmentStatus;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.MyCourseRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

/**
 * 사용자(User) 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 현재 로그인한 사용자의 정보 조회, 수정 및 수강 목록 조회를 담당합니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 28.
 */
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final EnrollmentRepository enrollmentRepository;

	/**
	 * 내 정보 조회
	 */
	@Transactional(readOnly = true)
	public UserProfileRes getMyProfile() {
		Long currentUserId = SecurityUtil.getCurrentUserId();

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		return UserProfileRes.from(user);
	}

	/**
	 * 내 정보 수정
	 */
	@Transactional
	public UserProfileRes updateMyProfile(UserUpdateReq req) {

		Long currentUserId = SecurityUtil.getCurrentUserId();

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		user.updateProfile(req.getName(), req.getPhoneNumber());
		return UserProfileRes.from(user);
	}

	/**
	 * 내 강의 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<MyCourseRes> getMyCourses(Long userId) {

		List<Enrollment> enrollments = enrollmentRepository.findAllByUserIdAndStatusOrderByCreatedAtDesc(
			userId, EnrollmentStatus.ENROLLED);

		return enrollments.stream()
			.map(enrollment -> MyCourseRes.builder()
				.courseId(enrollment.getCourse().getId())
				.title(enrollment.getCourse().getTitle())
				.thumbnail(enrollment.getCourse().getThumbnailUrl())
				.lecturerName(enrollment.getCourse().getLecturer().getName())
				.price(enrollment.getCourse().getPrice())
				.enrolledAt(enrollment.getCreatedAt())
				.build())
			.toList();
	}
}