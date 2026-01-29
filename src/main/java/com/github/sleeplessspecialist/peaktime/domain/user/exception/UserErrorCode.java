package com.github.sleeplessspecialist.peaktime.domain.user.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UserErrorCode Enum 클래스입니다.
 * <p>
 * 사용자(User) 도메인에서 발생하는 예외 상황에 대한
 * HTTP 상태 코드와 에러 메시지를 정의합니다.
 * </p>
 *
 * @author 기섭, 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND("U001", "사용자를 찾을 수 없습니다.", HttpStatus.NOT_FOUND),
	DUPLICATE_EMAIL("U002", "이미 존재하는 이메일입니다.", HttpStatus.CONFLICT),
	PASSWORD_NOT_MATCH("U003", "비밀번호가 일치하지 않습니다.", HttpStatus.BAD_REQUEST),
	ALREADY_DELETED_USER("U004", "이미 탈퇴한 회원입니다.", HttpStatus.BAD_REQUEST),
  INVALID_STATUS_TRANSITION("U400", "INVALID_STATUS_TRANSITION", HttpStatus.BAD_REQUEST);


	private final String code;
	private final String message;
	private final HttpStatus httpStatus;
}