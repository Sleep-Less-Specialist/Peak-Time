package com.github.sleeplessspecialist.peaktime.domain.review.controller;


import com.github.sleeplessspecialist.peaktime.domain.review.dto.*;
import com.github.sleeplessspecialist.peaktime.domain.review.service.ReviewService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;
import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

/**
 * 리뷰 관련 API를 제공하는 컨트롤러입니다.
 * <p>
 * 수강 이력(Enrollment)을 기반으로 리뷰를 생성합니다.
 * 한 Enrollment 당 리뷰 1개 제약은 서비스/DB 레벨에서 함께 보장됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1")
public class ReviewController {

	private final ReviewService reviewService;

    /**
     * 수강 생성
     */
    @PostMapping("/enrollments/{enrollmentId}/reviews")
	public ApiResponse<CreateReviewRes> createReview(
            @AuthenticationPrincipal Long userId,
            @Min(1) @PathVariable Long enrollmentId,
            @Valid @RequestBody CreateReviewReq request) {

        CreateReviewRes response = reviewService.createReview(userId, enrollmentId, request);
        return ApiResponse.of(SuccessCode.CREATED, response);
    }

    /**
     * 리뷰 상세 조회
     */
    @GetMapping("/reviews/{reviewId}")
    public ApiResponse<GetReviewDetailRes> getReviewDetail(
            @AuthenticationPrincipal Long userId,
            @Min(1) @PathVariable Long reviewId
    ) {
        GetReviewDetailRes response = reviewService.getReviewDetail(userId, reviewId);
        return ApiResponse.of(SuccessCode.OK, response);
    }

    /**
     * 리뷰 목록 조회(내 리뷰)
     */
    @GetMapping("/reviews")
    public ApiResponse<GetReviewListRes> getMyReviews(
            @AuthenticationPrincipal Long userId,
            @Min(1) @RequestParam(defaultValue = "1") int page,
            @Min(1) @Max(50) @RequestParam(defaultValue = "10") int size) {

        GetReviewListRes response = reviewService.getMyReviews(userId, page, size);
        return ApiResponse.of(SuccessCode.OK, response);
    }

    /**
     * 리뷰 수정
     */
    @PutMapping("/reviews/{reviewId}")
    public ApiResponse<UpdateReviewRes> updateReview(
            @AuthenticationPrincipal Long userId,
            @Min(1) @PathVariable Long reviewId,
            @Valid @RequestBody UpdateReviewReq request
    ) {
        UpdateReviewRes response = reviewService.updateReview(userId, reviewId, request);
        return ApiResponse.of(SuccessCode.OK, response);
    }

    /**
     * 리뷰 삭제
     */
    @DeleteMapping("/reviews/{reviewId}")
    public ApiResponse<Void> deleteReview(
            @AuthenticationPrincipal Long userId,
            @Min(1) @PathVariable Long reviewId
    ) {
        reviewService.deleteReview(userId, reviewId);
        return ApiResponse.ok();
    }
}
