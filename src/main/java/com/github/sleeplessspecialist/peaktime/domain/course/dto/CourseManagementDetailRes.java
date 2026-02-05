package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 지식공유자 전용 강의 상세 조회 응답 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 5.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED, force = true)
public class CourseManagementDetailRes {

	private final Long courseId;
	private final String title;
	private final String description;
	private final String category;
	private final BigDecimal price;
	private final String thumbnailUrl;
	private final Double ratingAvg;
	private final Integer reviewCount;
	private final String lecturerName;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
}
