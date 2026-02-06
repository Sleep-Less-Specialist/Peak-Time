package com.github.sleeplessspecialist.peaktime.domain.user.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;

@Builder
public record MyCourseRes(
	Long courseId,
	String title,
	String thumbnail,
	String lecturerName,
	BigDecimal price,
	LocalDateTime enrolledAt
) {
}