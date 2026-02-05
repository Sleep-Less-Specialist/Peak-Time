package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리뷰 수정 응답 DTO입니다.
 * <p>
 * 리뷰 수정 결과를 반환합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.04
 */
@Getter
@RequiredArgsConstructor
@Builder
public class UpdateReviewRes {

    private final Long reviewId;
    private final Long userId;
    private final Long enrollmentId;
    private final Long courseId;
    private final Integer rating;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
