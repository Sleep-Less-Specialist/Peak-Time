package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 지식공유자 전용 강의 상세 조회 응답 DTO
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 5.
 */
@Builder
public record CourseManagementDetailRes(
	Long courseId,
	String title,
	String description,
	String category,
	BigDecimal price,
	String thumbnailUrl,
	Double ratingAvg,
	Integer reviewCount,
	String lecturerName,
	LocalDateTime createdAt,
	LocalDateTime updatedAt
) {
}