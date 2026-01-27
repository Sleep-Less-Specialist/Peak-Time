package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 등록 성공 시 반환되는 응답 DTO입니다.
 * <p>
 * 등록된 강의의 식별자(ID)를 클라이언트에게 전달하여,
 * 이후 강의 상세 조회나 영상 업로드 요청 등에 사용할 수 있도록 합니다.
 * </p>
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