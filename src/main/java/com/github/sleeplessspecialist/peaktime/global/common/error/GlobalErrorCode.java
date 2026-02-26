package com.github.sleeplessspecialist.peaktime.global.common.error;

import org.springframework.http.HttpStatus;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 애플리케이션 전역에서 공통으로 사용되는 에러 코드를 정의한 enum입니다.
 *
 * <p>
 * 특정 도메인(auth, user, admin 등)에 종속되지 않는 요청 오류 및 시스템 오류를 표현합니다.
 * </p>
 *
 * <p>
 * 도메인 특화 에러는 각 도메인별 ErrorCode(enum)로 분리하여 관리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
@RequiredArgsConstructor
public enum GlobalErrorCode implements ErrorCode {

    INVALID_REQUEST("G400", "INVALID_REQUEST", HttpStatus.BAD_REQUEST),
    UNAUTHORIZED("G401", "UNAUTHORIZED", HttpStatus.UNAUTHORIZED),
    ACCESS_DENIED("G403", "ACCESS_DENIED", HttpStatus.FORBIDDEN),
    DATA_INTEGRITY_VIOLATION("G409", "DATA_INTEGRITY_VIOLATION", HttpStatus.CONFLICT),
    INTERNAL_SERVER_ERROR("G500", "INTERNAL_SERVER_ERROR", HttpStatus.INTERNAL_SERVER_ERROR),
    LOCK_ACQUISITION_FAILED("G410", "LOCK_ACQUISITION_FAILED", HttpStatus.CONFLICT);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}