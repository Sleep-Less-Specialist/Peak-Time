package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 지식공유자(Lecturer) 관련 강의 관리 API를 제공하는 컨트롤러입니다.
 * <p>
 * 강의 등록, 수정, 삭제 및 지식공유자 본인의 강의 목록 조회 기능을 담당합니다.
 * 모든 요청은 지식공유자 권한(ROLE_LECTURER)이 필요합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lecturer/courses")
public class CourseController {

	private final CourseService courseService;

	/**
	 * 신규 강의를 등록합니다.
	 *
	 * @param req 강의 등록 요청 정보 (제목, 설명, 가격 등)
	 * @return 등록된 강의 ID를 포함한 응답 객체
	 */
	@PostMapping
	public ApiResponse<CourseRegisterRes> registerCourse(@RequestBody CourseRegisterReq req) {

		CourseRegisterRes response = courseService.registerCourse(req);
		return ApiResponse.ok(response);
	}

	/**
	 * 강의 상세 정보를 조회합니다.
	 * <p>
	 * 접근 권한: 누구나 가능 (비로그인 포함)
	 * </p>
	 *
	 * @param courseId 조회할 강의 ID
	 * @return 강의 상세 정보 (커리큘럼 포함, 영상 URL 제외)
	 */
	@GetMapping("/{courseId}")
	public ApiResponse<CourseDetailRes> getCourseDetail(@PathVariable Long courseId) {

		CourseDetailRes response = courseService.getCourseDetail(courseId);
		return ApiResponse.ok(response);
	}
}