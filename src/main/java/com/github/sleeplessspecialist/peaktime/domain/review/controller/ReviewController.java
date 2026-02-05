package com.github.sleeplessspecialist.peaktime.domain.review.controller;


import com.github.sleeplessspecialist.peaktime.domain.review.dto.CreateReviewReq;
import com.github.sleeplessspecialist.peaktime.domain.review.dto.CreateReviewRes;
import com.github.sleeplessspecialist.peaktime.domain.review.service.ReviewService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;
import jakarta.validation.constraints.Min;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;


import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

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
@Validated
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
}
