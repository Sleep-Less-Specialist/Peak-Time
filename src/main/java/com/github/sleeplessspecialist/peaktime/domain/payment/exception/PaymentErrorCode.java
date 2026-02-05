package com.github.sleeplessspecialist.peaktime.domain.payment.exception;

/**
 *  결제(Payment) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum PaymentErrorCode implements ErrorCode {

	USER_NOT_FOUND("PM001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	ORDER_NOT_FOUND("PM002", "존재하지 않는 주문입니다.", HttpStatus.NOT_FOUND),
	PAYMENT_NOT_FOUND("PM003", "존재하지 않는 결제입니다.", HttpStatus.NOT_FOUND),
	INVALID_ORDER_STATUS("PM004","결제 상태 전이 실패." , HttpStatus.CONFLICT);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
