package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.time.LocalDateTime;

/**
 * 리뷰 상세 조회 응답 DTO입니다.
 * <p>
 * 리뷰 단건 조회 시, 리뷰 본문/평점과 함께
 * 어떤 수강 이력(enrollment)과 강의(course)에 속한 리뷰인지 식별할 수 있는 최소 정보를 제공합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.04
 */
@Getter
@RequiredArgsConstructor
@Builder
public class GetReviewDetailRes {

    private final Long reviewId;
    private final Long userId;
    private final Long enrollmentId;
    private final Long courseId;
    private final Integer rating;
    private final String content;
    private final LocalDateTime createdAt;
    private final LocalDateTime updatedAt;
}
