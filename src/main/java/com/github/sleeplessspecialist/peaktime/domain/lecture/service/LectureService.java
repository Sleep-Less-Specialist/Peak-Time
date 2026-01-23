package com.github.sleeplessspecialist.peaktime.domain.lecture.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.lecture.dto.LectureCreateReq;
import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.lecture.exception.LectureErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.lecture.repository.LectureRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;
import com.github.sleeplessspecialist.peaktime.global.infra.s3.S3Uploader;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 강의 영상 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 전달받은 영상 파일을 S3에 업로드하고, 반환된 URL을 이용해
 * Lecture 엔티티를 생성 및 저장하는 역할을 수행합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;
	private final CourseRepository courseRepository;
	private final S3Uploader s3Uploader;

	// 허용할 비디오 확장자 목록
	private static final List<String> ALLOWED_VIDEO_EXTENSIONS = List.of("mp4", "avi", "mov", "wmv", "mkv");

	/**
	 * 특정 과정(Course)에 새로운 강의 영상을 등록합니다.
	 * S3 업로드(네트워크 I/O)는 트랜잭션 외부에서 수행합니다.
	 *
	 * @param courseId 영상을 등록할 과정의 식별자 ID
	 * @param req      영상 제목과 파일 데이터가 담긴 요청 객체
	 * @return 저장된 강의 영상의 식별자 ID
	 * @throws CustomException 과정이 존재하지 않거나 업로드에 실패한 경우 발생
	 */
	public Long createLecture(Long courseId, LectureCreateReq req) {
		Course course = courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(GlobalErrorCode.INVALID_REQUEST));

		// 파일 확장자 검증
		validateVideoFileExtension(req.getVideoFile());

		// S3 업로드 (폴더명: video)
		String videoUrl = s3Uploader.upload(req.getVideoFile(), "video");

		try {
			// DB 저장 (별도 트랜잭션)
			return saveLectureMetadata(course, req.getTitle(), videoUrl);
		} catch (Exception e) {
			// 보상 트랜잭션, DB 저장 실패 시 S3에 올라간 파일 삭제
			log.error("DB 저장 실패로 인한 S3 파일 롤백 수행. url={}", videoUrl);
			s3Uploader.deleteFile(videoUrl);
			throw e;
		}
	}

	/**
	 * S3 업로드중엔 DB 트랜잭션이 일어나지 않아야함
	 * 실제 DB 저장을 담당하는 트랜잭션 메서드
	 */
	@Transactional
	public Long saveLectureMetadata(Course course, String title, String videoUrl) {
		Lecture lecture = Lecture.builder()
			.title(title)
			.videoUrl(videoUrl)
			.course(course)
			.build();

		return lectureRepository.save(lecture).getId();
	}

	/**
	 * 업로드된 파일의 확장자가 허용된 비디오 형식인지 검증함
	 */
	private void validateVideoFileExtension(MultipartFile file) {
		String originalFilename = file.getOriginalFilename();

		// 1. 파일명 자체가 문제가 있는 경우
		if (originalFilename == null || !originalFilename.contains(".")) {
			throw new CustomException(LectureErrorCode.INVALID_FILE_NAME);
		}

		String extension = originalFilename.substring(originalFilename.lastIndexOf(".") + 1).toLowerCase();

		// 2. 허용되지 않은 확장자인 경우
		if (!ALLOWED_VIDEO_EXTENSIONS.contains(extension)) {
			throw new CustomException(LectureErrorCode.INVALID_FILE_EXTENSION);
		}
	}
}