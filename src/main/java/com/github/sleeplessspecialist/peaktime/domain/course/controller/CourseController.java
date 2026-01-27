package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.web.PageableDefault;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 강의(Course) 조회 API 컨트롤러입니다.
 * <p>
 * 강의 목록 조회 및 상세 조회 기능을 제공하며,
 * 인증 여부와 관계없이 누구나 접근 가능합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 27.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/courses")
public class CourseController {

	private final CourseService courseService;

	/**
	 * 강의 목록 조회 API
	 * <p>
	 * 페이징 기능을 제공하며, 기본적으로 최신순(created_at DESC)으로 정렬됩니다.
	 * 예: /api/v1/courses?page=0&size=10
	 * </p>
	 *
	 * @param pageable 페이징 정보 (자동 주입)
	 * @return 페이징된 강의 목록
	 */
	@GetMapping
	public ApiResponse<Page<CourseListRes>> getCourseList(
		@PageableDefault(size = 10, sort = "createdAt", direction = Sort.Direction.DESC) Pageable pageable
	) {

		Page<CourseListRes> response = courseService.getCourseList(pageable);
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