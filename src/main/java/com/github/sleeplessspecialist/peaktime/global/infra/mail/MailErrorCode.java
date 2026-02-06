package com.github.sleeplessspecialist.peaktime.global.infra.mail;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 메일(SMTP) 연동 과정에서 발생하는 실패 케이스를 정의하는 에러 코드 enum입니다.
 *
 * <p>
 * 외부 SMTP 서버(Gmail)와의 네트워크 I/O 과정에서 지연/실패가 발생할 수 있으므로,
 * 공통 예외 처리 규약({@link ErrorCode})에 맞춰 HTTP 상태와 메시지를 일관되게 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 3.
 */
@Getter
@RequiredArgsConstructor
public enum MailErrorCode implements ErrorCode {

	MAIL_SEND_FAILED("M500", "메일 발송에 실패했습니다.", HttpStatus.INTERNAL_SERVER_ERROR);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

}