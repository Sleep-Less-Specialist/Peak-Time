package com.github.sleeplessspecialist.peaktime.domain.review.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * 리뷰 생성 요청 DTO입니다.
 * <p>
 * 특정 수강 이력(enrollmentId)에 대해 평점과 리뷰 내용을 전달합니다.
 * 평점 범위(예: 1~5) 및 내용 유효성은 Bean Validation으로 1차 검증합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
@Getter
@RequiredArgsConstructor
public class CreateReviewReq {

	@Min(value = 1)
	@Max(value = 5)
	private final int rating;

	@NotBlank(message = "content는 필수입니다.")
	private final String content;
}
