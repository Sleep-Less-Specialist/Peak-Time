package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 목록 조회 응답 DTO입니다.
 * <p>
 * 리스트 형태이므로 커리큘럼 같은 상세 정보는 제외하고,
 * 썸네일, 제목, 가격, 강사명 등 요약 정보만 포함합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 27.
 */
@Getter
@RequiredArgsConstructor
public class CourseListRes {

	private final Long courseId;
	private final String title;
	private final String description;
	private final String category;
	private final BigDecimal price;
	private final String thumbnailUrl;
	private final Double ratingAvg;
	private final Integer reviewCount;
	private final String lecturerName;
}