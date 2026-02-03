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

	/**
	 * ErrorCode만으로 CustomException을 생성합니다.
	 *
	 * <p>
	 * 원인(cause)을 별도로 보존할 필요가 없는 비즈니스 예외에서 사용합니다.
	 * 예외 메시지는 {@link ErrorCode#getMessage()}를 사용합니다.
	 * </p>
	 *
	 * @param errorCode 실패 응답 생성을 위한 에러 코드
	 */
	public CustomException(ErrorCode errorCode) {
		super(errorCode.getMessage());
		this.errorCode = errorCode;
	}

	/**
	 * ErrorCode와 원인 예외(cause)를 함께 포함하여 CustomException을 생성합니다.
	 *
	 * <p>
	 * 외부 시스템 연동(SMTP, 외부 API 등)에서 발생한 예외를 프로젝트의 공통 에러 규약(ErrorCode)으로
	 * 래핑할 때 사용합니다. cause를 보존하여 로그/트레이스에서 실제 원인을 추적할 수 있습니다.
	 * </p>
	 *
	 * @param errorCode 실패 응답 생성을 위한 에러 코드
	 * @param cause 실제 실패 원인 예외(스택트레이스 보존)
	 */
	public CustomException(ErrorCode errorCode, Throwable cause) {
		super(errorCode.getMessage(), cause);
		this.errorCode = errorCode;
	}

}