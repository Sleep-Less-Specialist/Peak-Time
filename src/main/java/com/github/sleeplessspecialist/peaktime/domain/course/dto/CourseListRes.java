package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

import lombok.Builder;

/**
 * 강의 전체 리스트 조회 응답 DTO
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 27.
 */
@Builder
public record CourseListRes(
	Long courseId,
	String title,
	String description,
	String category,
	BigDecimal price,
	String thumbnailUrl,
	Double ratingAvg,
	Integer reviewCount,
	String lecturerName
) {
}