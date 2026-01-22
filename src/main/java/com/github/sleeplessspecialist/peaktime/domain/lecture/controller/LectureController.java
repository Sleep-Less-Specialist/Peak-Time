package com.github.sleeplessspecialist.peaktime.domain.lecture.controller;

import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.lecture.dto.LectureCreateReq;
import com.github.sleeplessspecialist.peaktime.domain.lecture.service.LectureService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 강의 영상 관련 API 요청을 처리하는 컨트롤러입니다.
 * <p>
 * 특정 과정(Course) 하위에 영상을 업로드하거나 관리하는 기능을 제공합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class LectureController {

	private final LectureService lectureService;

	/**
	 * 강의 영상을 업로드하고 등록합니다.
	 * <p>
	 * `multipart/form-data` 형식을 사용하여 파일과 텍스트 데이터를 함께 전송받습니다.
	 * </p>
	 *
	 * @param courseId 영상을 등록할 과정의 ID
	 * @param req      업로드할 영상 파일과 제목 정보 (ModelAttribute 바인딩)
	 * @return 등록된 강의 영상의 ID
	 */
	@PostMapping(value = "/{courseId}/lectures", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<Long> uploadLecture(
		@PathVariable Long courseId,
		@Valid @ModelAttribute LectureCreateReq req) {

		Long lectureId = lectureService.createLecture(courseId, req);
		return ApiResponse.ok(lectureId);
	}
}