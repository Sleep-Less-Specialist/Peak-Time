package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;

import lombok.Getter;

/**
 * JWT 토큰 처리 과정에서 발생하는 예외를 표현하는 클래스입니다.
 * <p>
 * JWT 검증/파싱 실패를 {@link JwtTokenErrorCode} 기반의 도메인 예외로 변환하여
 * Security 계층에서 일관되게 처리하기 위해 사용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
public class JwtTokenException extends RuntimeException {

	private final JwtTokenErrorCode errorCode;

	/**
	 * JWT 토큰 처리 중 발생한 예외를 생성합니다.
	 *
	 * @param errorCode JWT 토큰 오류 코드
	 * @param cause     원인이 되는 하위 예외
	 */
	public JwtTokenException(JwtTokenErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

	/**
	 * 원인(cause) 없이 JWT 토큰 예외를 생성합니다.
	 *
	 * @param errorCode JWT 토큰 오류 코드
	 */
	public JwtTokenException(JwtTokenErrorCode errorCode) {
		this(errorCode, null);
	}
}