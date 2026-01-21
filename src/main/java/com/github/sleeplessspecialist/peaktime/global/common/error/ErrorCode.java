package com.github.sleeplessspecialist.peaktime.global.common.error;

import org.springframework.http.HttpStatus;

/**
 * API 실패 응답에 사용되는 공통 에러 코드 규약 인터페이스입니다.
 *
 * <p>
 * 모든 도메인별 에러 코드(enum)는 본 인터페이스를 구현하며,
 * 전역 예외 처리기(GlobalExceptionHandler)는 ErrorCode 타입을 기준으로
 * HTTP 상태 코드와 응답 메시지를 결정합니다.
 * </p>
 *
 * <p>
 * 이를 통해 도메인별 에러 코드가 추가되더라도 예외 처리 로직을 수정하지 않고 확장할 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
public interface ErrorCode {

	String getCode();

	String getMessage();

	HttpStatus getHttpStatus();

}