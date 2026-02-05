package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 전체 리스트 조회 응답 DTO입니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 27.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseListRes {

	private Long courseId;
	private String title;
	private String description;
	private String category;
	private BigDecimal price;
	private String thumbnailUrl;
	private Double ratingAvg;
	private Integer reviewCount;
	private String lecturerName;
}