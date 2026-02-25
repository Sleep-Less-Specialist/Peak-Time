package com.github.sleeplessspecialist.peaktime.domain.user.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.EnrollmentStatus;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.MyCourseRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.ProfileImageRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.ProfileImage;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.ProfileImageRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.s3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 사용자(User) 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 현재 로그인한 사용자의 정보 조회, 수정 및 수강 목록 조회를 담당합니다.
 * 프로필 이미지 업로드
 *
 * @author 기섭
 * @version 1.2
 * @since 2026. 1. 28.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class UserService {

	private final UserRepository userRepository;
	private final ProfileImageRepository profileImageRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final S3Uploader s3Uploader;

	private static final long MAX_IMAGE_SIZE = 5 * 1024 * 1024;

	/**
	 * 내 정보 조회
	 */
	@Transactional(readOnly = true)
	public UserProfileRes getMyProfile(Long userId) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		return UserProfileRes.from(user);
	}

	/**
	 * 내 정보 수정
	 */
	@Transactional
	public UserProfileRes updateMyProfile(Long userId, UserUpdateReq req) {

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		user.updateProfile(req.getName(), req.getPhoneNumber());
		return UserProfileRes.from(user);
	}

	/**
	 * 내 강의 목록 조회
	 */
	@Transactional(readOnly = true)
	public List<MyCourseRes> getMyCourses(Long userId) {

		List<Enrollment> enrollments =
			enrollmentRepository.findAllWithCourseAndLecturerByUserIdAndStatus(userId, EnrollmentStatus.ENROLLED);

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

	/**
	 * 프로필 이미지 업로드
	 * 1. S3 업로드
	 * 2. 기존 대표 이미지 해제 (History 유지)
	 * 3. 새 이미지 대표 설정 및 저장
	 */
	@Transactional
	public ProfileImageRes uploadProfileImage(Long userId, MultipartFile file) {

		validateProfileImage(file);

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		String dirName = "users/" + userId + "/profile";
		String imageUrl = s3Uploader.upload(file, dirName);

		try {
			profileImageRepository.findByUserIdAndIsPrimaryTrue(userId)
				.ifPresent(oldImage -> oldImage.setPrimary(false));

			ProfileImage newImage = ProfileImage.builder()
				.url(imageUrl)
				.isPrimary(true)
				.user(user)
				.build();

			profileImageRepository.save(newImage);

		} catch (RuntimeException e) {
			log.error("DB 저장 실패로 인한 S3 이미지 삭제: {}", imageUrl);
			s3Uploader.deleteFile(imageUrl);

			throw e;
		}

		return ProfileImageRes.builder()
			.imageUrl(imageUrl)
			.build();
	}

	/**
	 * 내부 메서드: 프로필 이미지 유효성 검사
	 * 1. 빈 파일 체크
	 * 2. 용량 체크 (5MB 제한)
	 * 3. 파일 형식 체크 (이미지 여부)
	 * * @param file 업로드된 파일
	 */
	private void validateProfileImage(MultipartFile file) {

		if (file.isEmpty()) {
			throw new CustomException(UserErrorCode.EMPTY_FILE_EXCEPTION);
		}

		if (file.getSize() > MAX_IMAGE_SIZE) {
			throw new CustomException(UserErrorCode.PROFILE_IMAGE_TOO_LARGE);
		}

		String contentType = file.getContentType();
		if (contentType == null || !contentType.startsWith("image")) {
			throw new CustomException(UserErrorCode.INVALID_PROFILE_IMAGE_TYPE);
		}
	}
}