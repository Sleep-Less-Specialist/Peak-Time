package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 상세 조회 시 포함되는 지식공유자(Lecturer) 정보 DTO입니다.
 * <p>
 * 이전의 Tutor라는 명칭을 제거하고 Lecturer로 통일했습니다.
 * </p>
 */
@Getter
@RequiredArgsConstructor
public class LecturerDto {

	private final Long lecturerId;
	private final String name;
}