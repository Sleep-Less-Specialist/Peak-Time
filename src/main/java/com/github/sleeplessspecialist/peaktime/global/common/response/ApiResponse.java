package com.github.sleeplessspecialist.peaktime.global.common.response;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 공통 성공 응답을 위한 표준 API 응답 래퍼 클래스입니다.
 *
 * <p>
 * 모든 컨트롤러의 성공 응답은 ApiResponse로 감싸서 반환하며,
 * 실패 응답은 GlobalExceptionHandler에서 ErrorResponse로 처리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@JsonInclude(JsonInclude.Include.NON_NULL)
public final class ApiResponse<T> {

	private final boolean success;
	private final String code;
	private final String message;
	private final T data;
	private final LocalDateTime timestamp;

	/**
	 * 데이터가 없는 기본 성공 응답
	 */
	public static <T> ApiResponse<T> ok() {
		return new ApiResponse<>(
			true,
			SuccessCode.OK.getCode(),
			SuccessCode.OK.getMessage(),
			null,
			LocalDateTime.now()
		);
	}

	/**
	 * 데이터를 포함한 기본 성공 응답
	 */
	public static <T> ApiResponse<T> ok(T data) {
		return new ApiResponse<>(
			true,
			SuccessCode.OK.getCode(),
			SuccessCode.OK.getMessage(),
			data,
			LocalDateTime.now()
		);
	}

	/**
	 * 리소스 생성 성공 응답 (201 Created)
	 */
	public static <T> ApiResponse<T> created(T data) {
		return new ApiResponse<>(
			true,
			SuccessCode.CREATED.getCode(),
			SuccessCode.CREATED.getMessage(),
			data,
			LocalDateTime.now()
		);
	}

	/**
	 * 데이터가 없는 성공 응답 (204 No Content)
	 *
	 * <p>
	 * 응답 바디를 포함하지 않는 성공 응답이 필요한 경우 사용합니다.
	 * (예: 로그아웃, 상태 변경 API 등)
	 * </p>
	 */
	public static <T> ApiResponse<T> noContent() {
		return new ApiResponse<>(
			true,
			SuccessCode.NO_CONTENT.getCode(),
			SuccessCode.NO_CONTENT.getMessage(),
			null,
			LocalDateTime.now()
		);
	}

	/**
	 * 커스텀 성공 코드 ( 데이터 없음)
	 */
	public static <T> ApiResponse<T> of(SuccessCode successCode) {
		return new ApiResponse<>(
			true,
			successCode.getCode(),
			successCode.getMessage(),
			null,
			LocalDateTime.now()
		);
	}

	/**
	 * 커스텀 성공 코드 + 데이터
	 */
	public static <T> ApiResponse<T> of(SuccessCode successCode, T data) {
		return new ApiResponse<>(
			true,
			successCode.getCode(),
			successCode.getMessage(),
			data,
			LocalDateTime.now()
		);
	}
}