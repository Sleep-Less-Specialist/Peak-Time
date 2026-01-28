package com.github.sleeplessspecialist.peaktime.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * <p>
 * TODO: enum의 역할과 각 상수의 의미를 작성하세요.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND("U004", "USER_NOT_FOUND", HttpStatus.NOT_FOUND),
	INVALID_STATUS_TRANSITION("U400", "INVALID_STATUS_TRANSITION", HttpStatus.BAD_REQUEST);


	private final String code;
	private final String message;
	private final HttpStatus httpStatus;



}