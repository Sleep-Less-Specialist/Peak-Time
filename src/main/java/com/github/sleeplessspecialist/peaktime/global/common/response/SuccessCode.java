package com.github.sleeplessspecialist.peaktime.global.common.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * API 성공 응답에 사용되는 비즈니스 성공 코드입니다.
 *
 * <p>
 * HTTP Status와 별도로, 요청이 비즈니스적으로 정상 처리되었음을 표현합니다.
 * 기본 성공 코드는 {@code S000}이며 HTTP 200 OK에 대응합니다.
 * </p>
 *
 * <p>
 * v1에서는 공통 성공 코드(OK)만 사용하며,
 * 도메인별 성공 코드가 필요해질 경우 분리할 수 있습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
@RequiredArgsConstructor
public enum SuccessCode {

	OK("S000", "OK"),
	CREATED("S001", "Created");

	private final String code;
	private final String message;

}