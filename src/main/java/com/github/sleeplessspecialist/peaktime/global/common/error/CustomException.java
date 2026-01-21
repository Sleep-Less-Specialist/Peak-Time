package com.github.sleeplessspecialist.peaktime.global.common.error;

import lombok.Getter;

/**
 * ErrorCode를 기반으로 한 공통 사용자 정의 예외 클래스입니다.
 * <p>
 * 모든 비즈니스 예외는 본 예외를 상속하거나 사용하여 발생시키며,
 * 전역 예외 처리기에서 ErrorCode를 기준으로 공통 실패 응답으로 변환됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
public class CustomException extends RuntimeException {

	private final ErrorCode errorCode;

	public CustomException(ErrorCode errorcode) {
		super(errorcode.getMessage());
		this.errorCode = errorcode;
	}

}