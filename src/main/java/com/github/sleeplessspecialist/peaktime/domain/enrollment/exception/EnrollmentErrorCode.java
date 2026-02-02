package com.github.sleeplessspecialist.peaktime.domain.enrollment.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *   강의 수강 등록(enrollment) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Getter
@RequiredArgsConstructor
public enum EnrollmentErrorCode implements ErrorCode {

	USER_NOT_FOUND("EN001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	ORDER_NOT_FOUND("EN002", "존재하지 않는 주문입니다.", HttpStatus.NOT_FOUND),
	INVALID_ENROLLMENT_STATUS("EN003", "강의 등록 상태 전이 실패.", HttpStatus.CONFLICT);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
