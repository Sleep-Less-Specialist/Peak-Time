package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 리뷰 생성/조회 응답 DTO입니다.
 * <p>
 * 클라이언트가 즉시 화면에 렌더링할 수 있도록 리뷰 식별자와 함께
 * 수강(enrollment), 강의(course), 작성자(user) 식별자 및 생성/수정 시각을 포함합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateReviewRes {

	private final Long id;
	private final Long enrollmentId;
	private final Long courseId;
	private final Long userId;
	private final Integer rating;
	private final String content;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
}
