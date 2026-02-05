package com.github.sleeplessspecialist.peaktime.domain.course.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의 상세 정보에 포함되는 지식공유자(Lecturer) 응답 DTO입니다
 */
@Getter
@RequiredArgsConstructor
public class LecturerDto {

	private final Long lecturerId;
	private final String name;
}