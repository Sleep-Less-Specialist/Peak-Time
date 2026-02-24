package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

/**
 * 강의 목록 검색을 위한 조건 DTO입니다.
 * <p>
 * 강의 목록 조회 API에서 전달받은 검색/정렬 파라미터를 한 객체로 묶어 전달합니다.
 * 미지정된 값은 null로 전달되며, Repository(QueryDSL)에서 동적으로 조건을 조합합니다.
 * </p>
 *
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 24.
 */
public record CourseSearchCondition(
	String category,
	String keyword,
	BigDecimal minPrice,
	BigDecimal maxPrice,
	CourseSortBy sortBy,
	SortDirection direction
) {
	public CourseSearchCondition {
		if (sortBy == null)
			sortBy = CourseSortBy.CREATED_AT;
		if (direction == null)
			direction = SortDirection.DESC;
	}

	public enum CourseSortBy {
		CREATED_AT, PRICE, RATING_AVG
	}

	public enum SortDirection {
		ASC, DESC
	}
}