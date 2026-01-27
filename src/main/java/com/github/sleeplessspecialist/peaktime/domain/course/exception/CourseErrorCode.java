package com.github.sleeplessspecialist.peaktime.domain.course.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 강의(Course) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum입니다.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public enum CourseErrorCode implements ErrorCode {

	USER_NOT_FOUND("C001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	UNAUTHORIZED_ACCESS("C002", "강의를 등록할 권한이 없습니다.", HttpStatus.FORBIDDEN),
	COURSE_NOT_FOUND("C003", "존재하지 않는 강의입니다.", HttpStatus.NOT_FOUND);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}