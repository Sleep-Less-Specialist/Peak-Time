package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * CourseManagementDetailRes 클래스입니다.
 * <p>
 * 강의의 모든 상세 정보와 관리용 데이터(등록일, 수정일 등)를 포함합니다.
 * </p>
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
