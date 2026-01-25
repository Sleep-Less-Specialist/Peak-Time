package com.github.sleeplessspecialist.peaktime.domain.point.exception;

import org.springframework.http.HttpStatus;

import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 포인트 도메인에서 발생하는 비즈니스 예외를 표현하는 에러코드 enum입니다.
 * <p>
 * 포인트 적립/차감 과정에서 정책 위반이나 처리 불가능한 상태가 발생한 경우
 * 서비스 레이어에서 {@link CustomException}와 함께 사용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 25.
 */
@Getter
@RequiredArgsConstructor
public enum PointErrorCode implements ErrorCode {

	INSUFFICIENT_POINT("P400", "INSUFFICIENT_POINT", HttpStatus.BAD_REQUEST),
	INVALID_POINT_AMOUNT("P401", "INVALID_POINT_AMOUNT", HttpStatus.BAD_REQUEST),
	DUPLICATE_POINT_TRANSACTION("P409", "DUPLICATE_POINT_TRANSACTION", HttpStatus.CONFLICT);

	private final String code;
	private final String message;
	private final HttpStatus httpStatus;

}