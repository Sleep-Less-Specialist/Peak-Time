package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 지식공유자(Lecturer) 전용 강의 관리 API 컨트롤러입니다.
 * <p>
 * 강의 등록, 수정, 삭제 등 지식공유자 권한(ROLE_LECTURER)이 필요한 기능을 담당합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 27.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lecturer/courses")
public class LecturerCourseController {

	private final CourseService courseService;

	/**
	 * 신규 강의를 등록합니다.
	 *
	 * @param req 강의 등록 요청 정보 (제목, 설명, 가격 등)
	 * @return 등록된 강의 ID를 포함한 응답 객체
	 */
	@PostMapping
	public ApiResponse<CourseRegisterRes> registerCourse(@RequestBody @Valid CourseRegisterReq req) {

		CourseRegisterRes response = courseService.registerCourse(req);
		return ApiResponse.created(response);
	}
}