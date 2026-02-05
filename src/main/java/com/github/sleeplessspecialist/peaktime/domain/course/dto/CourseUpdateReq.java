package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 정보를 수정할 때 사용하는 요청 DTO입니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 2. 3.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CourseUpdateReq {

	@NotBlank(message = "강의 제목은 필수입니다.")
	private String title;

	@NotBlank(message = "강의 설명은 필수입니다.")
	private String description;

	@NotBlank(message = "카테고리는 필수입니다.")
	private String category;

	@NotNull(message = "가격은 필수입니다.")
	@Min(value = 0, message = "가격은 0원 이상이어야 합니다.")
	private BigDecimal price;

	private String thumbnailUrl;
}