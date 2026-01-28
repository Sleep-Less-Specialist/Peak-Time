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
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Getter
@RequiredArgsConstructor
public enum UserErrorCode implements ErrorCode {

	USER_NOT_FOUND("U001", HttpStatus.NOT_FOUND, "사용자를 찾을 수 없습니다."),
	DUPLICATE_EMAIL("U002", HttpStatus.CONFLICT, "이미 존재하는 이메일입니다."),
	PASSWORD_NOT_MATCH("U003", HttpStatus.BAD_REQUEST, "비밀번호가 일치하지 않습니다."),
	ALREADY_DELETED_USER("U004", HttpStatus.BAD_REQUEST, "이미 탈퇴한 회원입니다.");

	private final String code;
	private final HttpStatus httpStatus;
	private final String message;
}