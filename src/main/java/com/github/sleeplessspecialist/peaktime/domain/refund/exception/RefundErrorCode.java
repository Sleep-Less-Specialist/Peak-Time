package com.github.sleeplessspecialist.peaktime.domain.refund.exception;

/**
 *  환불(Refund) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.31
 */

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum RefundErrorCode implements ErrorCode {

	ALREADY_REFUNDED("RF001", "이미 환불 내역이 존재 합니다.", HttpStatus.CONFLICT);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
