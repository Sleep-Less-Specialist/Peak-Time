package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리뷰 수정 요청 DTO입니다.
 * <p>
 * 수정 가능한 필드(평점, 내용)를 전달합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.04
 */
@Getter
@RequiredArgsConstructor
@Builder
public class UpdateReviewReq {

    @Min(1)
    @Max(5)
    private final Integer rating;

    @NotBlank
    private final String content;
}