package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 등록 요청 시 클라이언트로부터 전달받는 데이터를 담는 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@NoArgsConstructor
public class CourseRegisterReq {

	private String title;
	private String description;
	private String category;
	private BigDecimal price;
	private String thumbnailUrl;

}