package com.github.sleeplessspecialist.peaktime.domain.lecture.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LectureErrorCode 클래스입니다.
 * <p>
 * TODO: 클래스의 역할을 작성하세요.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

	// L001: 확장자가 없거나 이상할 때
	INVALID_FILE_EXTENSION("L001", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),

	// L002: 파일 이름 자체가 비어있거나 문제 있을 때
	INVALID_FILE_NAME("L002", "파일 이름이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
