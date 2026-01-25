package com.github.sleeplessspecialist.peaktime.domain.auth.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증/인가 도메인에서 사용하는 에러 코드를 정의한 enum입니다.
 * <p>
 * 회원가입/로그인 등 auth 도메인에서 발생하는 요청 오류를 표현합니다.
 * 전역 공통 오류는 {@link GlobalErrorCode}를 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 24.
 */
@Getter
@RequiredArgsConstructor
public enum AuthErrorCode implements ErrorCode {

	VALIDATION_ERROR("A400", "VALIDATION_ERROR", HttpStatus.BAD_REQUEST),
	INVALID_PHONE_NUMBER("A401", "INVALID_PHONE_NUMBER", HttpStatus.BAD_REQUEST),
	EMAIL_ALREADY_EXISTS("A409", "EMAIL_ALREADY_EXISTS", HttpStatus.CONFLICT);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

}