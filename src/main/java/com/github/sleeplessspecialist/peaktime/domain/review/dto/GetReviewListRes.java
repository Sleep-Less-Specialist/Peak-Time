package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 리뷰 목록 조회 응답 DTO입니다.
 * <p>
 * 리뷰 목록과 페이징 정보를 함께 반환합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.04
 */
@Getter
@RequiredArgsConstructor
@Builder
public class GetReviewListRes {

    private final List<ReviewListItemRes> reviews;

    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
}
