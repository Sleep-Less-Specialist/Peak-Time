package com.github.sleeplessspecialist.peaktime.domain.lecture.service;

import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.lecture.dto.LectureCreateReq;
import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.lecture.repository.LectureRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;
import com.github.sleeplessspecialist.peaktime.global.infra.s3.S3Uploader;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;

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
@Service
@RequiredArgsConstructor
public class LectureService {

	private final LectureRepository lectureRepository;
	private final CourseRepository courseRepository;
	private final S3Uploader s3Uploader;

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

		// S3 업로드 (폴더명: video)
		String videoUrl = s3Uploader.upload(req.getVideoFile(), "video");

		return saveLectureMetadata(course, req.getTitle(), videoUrl);
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
}