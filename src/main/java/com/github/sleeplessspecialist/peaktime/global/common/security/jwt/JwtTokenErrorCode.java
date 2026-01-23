package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorResponse;
import com.github.sleeplessspecialist.peaktime.global.common.security.handler.RestAuthenticationEntryPoint;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 토큰 검증 과정에서 발생하는 에러 코드를 정의합니다.
 * <p>
 * 인증(Authentication) 단계에서 발생하는 오류에 한정되며,
 * {@link RestAuthenticationEntryPoint} 에서 공통 {@link ErrorResponse} 로 변환됩니다.
 * </p>
 *
 * <p>
 * 본 enum은 JWT 토큰 자체의 상태(만료, 위변조, 형식 오류 등)를 표현합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public enum JwtTokenErrorCode implements ErrorCode {

	EXPIRED("JWT_001", "만료된 토큰입니다.", HttpStatus.UNAUTHORIZED),
	INVALID_SIGNATURE("JWT_002", "토큰 서명이 유효하지 않습니다.", HttpStatus.UNAUTHORIZED),
	MALFORMED("JWT_003", "토큰 형식이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
	UNSUPPORTED("JWT_004", "지원하지 않는 토큰 형식입니다.", HttpStatus.UNAUTHORIZED),
	EMPTY("JWT_005", "토큰이 비어있습니다.", HttpStatus.UNAUTHORIZED),
	INVALID("JWT_006", "유효하지 않은 토큰입니다.", HttpStatus.UNAUTHORIZED),
	INVALID_SUBJECT("JWT_007", "토큰 subject(userId) 형식이 올바르지 않습니다.", HttpStatus.UNAUTHORIZED),
	INVALID_SECRET("JWT_008", "JWT secret 설정이 올바른 Base64 형식이 아닙니다. "
		+ "설정 값(jwt.secret 또는 환경변수 JWT_SECRET_KEY)을 확인해주세요.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
