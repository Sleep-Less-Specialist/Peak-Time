package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import com.github.sleeplessspecialist.peaktime.domain.review.entity.Review;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

/**
 * 리뷰 목록 아이템 DTO입니다.
 * <p>
 * 목록에서 필요한 최소 정보(리뷰/수강/강의 식별자, 평점, 내용, 작성/수정일)를 제공합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.04
 */
@Getter
@Builder
public class ReviewListItemRes {

    private final Long reviewId;
    private final Long enrollmentId;
    private final Long courseId;

    private final Integer rating;
    private final String content;

    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;

    public static ReviewListItemRes from(Review review) {
        return ReviewListItemRes.builder()
                .reviewId(review.getId())
                .enrollmentId(review.getEnrollment().getId())
                .courseId(review.getEnrollment().getCourse().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }
}
