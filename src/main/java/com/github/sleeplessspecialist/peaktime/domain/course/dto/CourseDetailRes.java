package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 상세 조회 응답 DTO입니다.
 * <p>
 * 강의 기본 정보와 지식공유자(Lecturer) 정보, 커리큘럼(Curriculum)을 포함합니다.
 * 모든 필드는 private final로 선언되어 불변성을 보장합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 27.
 */
@Getter
@RequiredArgsConstructor
public class CourseDetailRes {

	private final Long courseId;
	private final String title;
	private final String description;
	private final BigDecimal price;
	private final String thumbnailUrl;
	private final Double ratingAvg;
	private final Integer reviewCount;
	private final LecturerDto lecturer;
	private final List<CurriculumDto> curriculum;
	private final LocalDateTime updatedAt;
}