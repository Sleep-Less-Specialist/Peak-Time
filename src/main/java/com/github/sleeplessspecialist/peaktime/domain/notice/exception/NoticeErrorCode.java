package com.github.sleeplessspecialist.peaktime.domain.notice.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Notice 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum입니다.
 *
 * <p>
 * 알림 생성/조회 과정에서 발생하는 예외를 정의하며,
 * GlobalExceptionHandler에서 해당 코드를 기반으로
 * 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 12.
 */
@Getter
@RequiredArgsConstructor
public enum NoticeErrorCode implements ErrorCode {

    ORDER_NOT_FOUND("N001", "주문 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    NOTICE_NOT_FOUND("N002", "알림 정보를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
    UNSUPPORTED_NOTICE_TYPE("N003", "지원하지 않는 알림 유형입니다.", HttpStatus.BAD_REQUEST);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
