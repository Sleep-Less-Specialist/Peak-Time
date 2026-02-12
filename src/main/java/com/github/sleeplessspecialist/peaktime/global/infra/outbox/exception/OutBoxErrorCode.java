package com.github.sleeplessspecialist.peaktime.global.infra.outbox.exception;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

/**
 * Outbox 이벤트 처리 과정에서 발생할 수 있는 예외 케이스를 정의하는 에러 코드 enum입니다.
 *
 * <p>
 * 멀티 인스턴스 환경, 재시도 로직, 멱등 처리 등으로 인해
 * 이벤트 중복 실행 및 잘못된 이벤트 데이터가 전달될 가능성이 있으므로,
 * 공통 예외 처리 규약({@link ErrorCode})에 맞춰 HTTP 상태와 메시지를 일관되게 정의합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 11.
 */
@Getter
@RequiredArgsConstructor
public enum OutBoxErrorCode implements ErrorCode {

    UNSUPPORTED_EVENT_TYPE("OB001", "지원하지 않는 Outbox 이벤트 타입입니다.", HttpStatus.BAD_REQUEST),
    INVALID_EVENT_DATA("OB002", "Outbox 이벤트 데이터가 유효하지 않습니다.", HttpStatus.BAD_REQUEST),
    EVENT_HANDLE_FAILED("OB003", "Outbox 이벤트 처리에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR),
    EVENT_ALREADY_PROCESSED("OB004", "이미 처리된 Outbox 이벤트입니다.", HttpStatus.CONFLICT),
    NOT_FOUND_EVENT("OB005", "Outbox 이벤트를 찾을수 없습니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
