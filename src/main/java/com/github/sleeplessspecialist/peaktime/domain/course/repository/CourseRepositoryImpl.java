package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import static com.github.sleeplessspecialist.peaktime.domain.course.entity.QCourse.*;
import static com.github.sleeplessspecialist.peaktime.domain.user.entity.QUser.*;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition.CourseSortBy;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition.SortDirection;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.dsl.BooleanExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;

import lombok.RequiredArgsConstructor;

/**
 * CourseRepositoryCustom의 QueryDSL 구현체입니다.
 * <p>
 * 강의 목록 조회 시 카테고리/키워드/가격 범위 등의 조건을 동적으로 조합하여 조회하고,
 * 정렬 기준(가격/최신/평점) 및 정렬 방향(ASC/DESC)을 적용한 결과를 페이징 형태로 반환합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 24.
 */
@RequiredArgsConstructor
public class CourseRepositoryImpl implements CourseRepositoryCustom {

	private final JPAQueryFactory queryFactory;

	@Override
	public Page<Course> searchCourses(CourseSearchCondition condition, Pageable pageable) {

		List<Course> content = queryFactory
			.selectFrom(course)
			.join(course.lecturer, user).fetchJoin()
			.where(
				categoryEq(condition.category()),
				keywordContains(condition.keyword()),
				minPriceGoe(condition.minPrice()),
				maxPriceLoe(condition.maxPrice())
			)
			.orderBy(orderBy(condition.sortBy(), condition.direction()))
			.offset(pageable.getOffset())
			.limit(pageable.getPageSize())
			.fetch();

		Long total = queryFactory
			.select(course.count())
			.from(course)
			.where(
				categoryEq(condition.category()),
				keywordContains(condition.keyword()),
				minPriceGoe(condition.minPrice()),
				maxPriceLoe(condition.maxPrice())
			)
			.fetchOne();

		long totalCount = (total == null) ? 0L : total;
		return new PageImpl<>(content, pageable, totalCount);
	}

	private BooleanExpression categoryEq(String category) {
		return (category == null || category.isBlank()) ? null : course.category.eq(category);
	}

	private BooleanExpression keywordContains(String keyword) {
		if (keyword == null || keyword.isBlank())
			return null;
		String k = keyword.trim();
		return course.title.containsIgnoreCase(k)
			.or(course.description.containsIgnoreCase(k));
	}

	private BooleanExpression minPriceGoe(java.math.BigDecimal minPrice) {
		return (minPrice == null) ? null : course.price.goe(minPrice);
	}

	private BooleanExpression maxPriceLoe(java.math.BigDecimal maxPrice) {
		return (maxPrice == null) ? null : course.price.loe(maxPrice);
	}

	private OrderSpecifier<?> orderBy(CourseSortBy sortBy, SortDirection direction) {
		Order order = (direction == SortDirection.ASC) ? Order.ASC : Order.DESC;

		CourseSortBy field = (sortBy == null) ? CourseSortBy.CREATED_AT : sortBy;

		return switch (field) {
			case PRICE -> new OrderSpecifier<>(order, course.price);
			case RATING_AVG -> new OrderSpecifier<>(order, course.ratingAvg);
			case CREATED_AT -> new OrderSpecifier<>(order, course.createdAt);
		};
	}
}
