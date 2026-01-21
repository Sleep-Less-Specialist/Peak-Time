package com.github.sleeplessspecialist.peaktime.global.common.error;

import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.Getter;

/**
 * API 실패 응답에 사용되는 공통 에러 응답 DTO입니다.
 *
 * <p>
 * 모든 실패 응답은 본 객체로 감싸서 반환되며, ErrorCode를 기반으로 응답 코드 및 메시지를 구성합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ErrorResponse {

	private final boolean success = false;
	private final String code;
	private final String message;
	private final List<ValidationFieldError> errors;
	private final LocalDateTime timestamp;

	private ErrorResponse(String code, String message, List<ValidationFieldError> errors) {
		this.code = code;
		this.message = message;

		if (errors == null) {
			this.errors = Collections.emptyList();
		} else {
			this.errors = errors;
		}

		this.timestamp = LocalDateTime.now();
	}

	/**
	 * ErrorCode 기반 기본 실패 응답
	 */
	public static ErrorResponse from(ErrorCode errorCode) {
		return new ErrorResponse(
			errorCode.getCode(),
			errorCode.getMessage(),
			Collections.emptyList()
		);
	}

	/**
	 * Validation 오류를 포함한 실패 응답
	 */
	public static ErrorResponse of(ErrorCode errorCode, List<ValidationFieldError> errors) {

		return new ErrorResponse(
			errorCode.getCode(),
			errorCode.getMessage(),
			errors
		);
	}
}