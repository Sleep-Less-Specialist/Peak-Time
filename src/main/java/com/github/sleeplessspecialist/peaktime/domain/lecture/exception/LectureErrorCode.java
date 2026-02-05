package com.github.sleeplessspecialist.peaktime.domain.lecture.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * LectureErrorCode 클래스입니다.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@RequiredArgsConstructor
public enum LectureErrorCode implements ErrorCode {

	INVALID_FILE_EXTENSION("L001", "지원하지 않는 파일 형식입니다.", HttpStatus.BAD_REQUEST),
	INVALID_FILE_NAME("L002", "파일 이름이 유효하지 않습니다.", HttpStatus.BAD_REQUEST);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
