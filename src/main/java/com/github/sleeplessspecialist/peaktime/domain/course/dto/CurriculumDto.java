package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 상세 조회 시 포함되는 커리큘럼(강의 영상 목차) 정보 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 27.
 */
@Getter
@RequiredArgsConstructor
public class CurriculumDto {

	private final Long lectureId;
	private final String title;
	private final Integer duration;
}
