package com.github.sleeplessspecialist.peaktime.domain.chat.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 커피쳇(chat) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum입니다.
 *
 * <<p>
 *  GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Getter
@RequiredArgsConstructor
public enum ChatErrorCode implements ErrorCode {

	USER_NOT_FOUND("CHAT-001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
	CHAT_ROOM_NOT_FOUND("CHAT-001", "존재하지 않는 채팅방입니다.", HttpStatus.NOT_FOUND),
	UNAUTHORIZED_ACCESS("CHAT-002", "사용자 권한이 부족합니다.", HttpStatus.FORBIDDEN),
	BAD_PAGING_CONDITION("CHAT-004", "페이징 형식이 올바르지 않습니다.", HttpStatus.BAD_REQUEST);


	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}
