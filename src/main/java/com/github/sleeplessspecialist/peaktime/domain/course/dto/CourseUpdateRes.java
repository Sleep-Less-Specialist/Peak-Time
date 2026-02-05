package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import lombok.Builder;
import lombok.Getter;

/**
 * 강의 수정 완료 후, 변경된 강의 정보를 클라이언트에게 반환하는 응답 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 5.
 */
@Getter
@Builder
public class CourseUpdateRes {

	private Long courseId;
	private String title;
	private String description;
	private String category;
	private BigDecimal price;
	private String thumbnailUrl;
	private LocalDateTime updatedAt;
}
