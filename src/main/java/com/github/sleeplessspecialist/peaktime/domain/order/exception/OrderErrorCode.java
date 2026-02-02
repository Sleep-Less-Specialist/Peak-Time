package com.github.sleeplessspecialist.peaktime.domain.order.exception;

/**
 *  강의(Order) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.02
 */

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum OrderErrorCode implements ErrorCode {

	USER_NOT_FOUND("OD001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	COURSE_NOT_FOUND("OD002", "존재하지 않는 강의입니다.", HttpStatus.NOT_FOUND),
	ORDER_NOT_FOUND("OD003", "존재하지 않는 주문입니다.", HttpStatus.NOT_FOUND),
	UNAUTHORIZED_ACCESS("OD004", "사용자 권한이 부족합니다.", HttpStatus.FORBIDDEN),
	INSUFFICIENT_POINT("OD005", "포인트가 충분하지 않습니다", HttpStatus.BAD_REQUEST),
	CANNOT_BUY_OWN_COURSE("OD006", "강사는 강의를 주문할수 없습니다.", HttpStatus.BAD_REQUEST),
	INVALID_USE_POINT("OD007", "사용 포인트가 결제 금액보다 클 수 없습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_ENROLLED_COURSE("OD008", "이미 수강 중이거나 수강 완료한 강의입니다.", HttpStatus.BAD_REQUEST),
	INVALID_ORDER_STATUS("OD009", "주문 상태가 올바르지 않습니다.", HttpStatus.BAD_REQUEST);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
