package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseRegisterRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseUpdateRes;
import com.github.sleeplessspecialist.peaktime.domain.course.service.LecturerCourseService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 지식공유자(Lecturer) 전용 강의 관리 API 컨트롤러입니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 27.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/lecturer/courses")
public class LecturerCourseController {

	private final LecturerCourseService lecturerCourseService;

	/**
	 * 신규 강의 등록
	 */
	@PostMapping
	public ApiResponse<CourseRegisterRes> registerCourse(
		@AuthenticationPrincipal Long userId,
		@RequestBody @Valid CourseRegisterReq req) {

		CourseRegisterRes response = lecturerCourseService.registerCourse(userId, req);
		return ApiResponse.created(response);
	}

	/**
	 * 강의 정보 수정
	 */
	@PutMapping("/{courseId}")
	public ApiResponse<CourseUpdateRes> updateCourse(
		@AuthenticationPrincipal Long userId,
		@PathVariable @Positive Long courseId,
		@RequestBody @Valid CourseUpdateReq req) {

		CourseUpdateRes response = lecturerCourseService.updateCourse(userId, courseId, req);
		return ApiResponse.ok(response);
	}

	/**
	 * 내 강의 목록 조회
	 */
	@GetMapping
	public ApiResponse<Page<CourseListRes>> getMyCourses(
		@AuthenticationPrincipal Long userId,
		@RequestParam(defaultValue = "1") int page,
		@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {

		Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		Page<CourseListRes> response = lecturerCourseService.getMyCourses(userId, pageable);
		return ApiResponse.ok(response);
	}
}