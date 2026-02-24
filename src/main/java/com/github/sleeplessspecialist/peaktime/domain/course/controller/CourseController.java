package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import java.math.BigDecimal;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.domain.ranking.dto.CourseRankingRes;
import com.github.sleeplessspecialist.peaktime.domain.ranking.service.CourseRankingService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

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
@Validated
@RequestMapping("/api/v1/courses")
public class CourseController {

	private final CourseService courseService;
    private final CourseRankingService rankingService;

	/**
	 * 강의 목록 조회 API
	 * <p>
	 * 페이징 기능을 제공하며, 기본적으로 최신순(created_at DESC)으로 정렬됩니다.
	 * page와 size를 명시적으로 받아서 검증(@Min)을 수행합니다.
	 * 클라이언트는 1페이지부터 요청한다고 가정하고, 서버에서는 0페이지로 변환합니다.
	 * </p>
	 *
	 * @return 페이징된 강의 목록
	 */
	@GetMapping
	public ApiResponse<Page<CourseListRes>> getCourseList(
		@RequestParam(defaultValue = "1") @Min(value = 1, message = "페이지는 1 이상이어야 합니다.") int page,
		@RequestParam(defaultValue = "10") @Min(value = 1, message = "사이즈는 1 이상이어야 합니다.") int size,

		@RequestParam(required = false) String category,
		@RequestParam(required = false) String keyword,
		@RequestParam(required = false) BigDecimal minPrice,
		@RequestParam(required = false) BigDecimal maxPrice,
		@RequestParam(required = false) CourseSearchCondition.CourseSortBy sortBy,
		@RequestParam(required = false) CourseSearchCondition.SortDirection direction
	) {
		if (minPrice != null && maxPrice != null && minPrice.compareTo(maxPrice) > 0) {
			throw new CustomException(CourseErrorCode.INVALID_PRICE_RANGE);
		}

		Pageable pageable = PageRequest.of(page - 1, size);

		CourseSearchCondition condition = new CourseSearchCondition(
			category, keyword, minPrice, maxPrice, sortBy, direction
		);

		Page<CourseListRes> response = courseService.getCourseList(condition, pageable);
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

    /**
     * 최근 N 일 인기 강의 TOP 랭킹 조회 API
     *
     * <p>
     * 결제(주문) 기반으로 Redis ZSET에 집계된 강의 인기 점수를 조회합니다.
     *
     * </p>
     */
    @GetMapping("/ranking/last-3-days")
    public ApiResponse<List<CourseRankingRes>> findCategoryLast3Days() {

        List<CourseRankingRes> response = rankingService.findTopCoursesInLast3Days();
        return ApiResponse.ok(response);
    }
}