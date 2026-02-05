package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 등록 성공 시 반환되는 응답 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public class CourseRegisterRes {

	private final Long courseId;
}