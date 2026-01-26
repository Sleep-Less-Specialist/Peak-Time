package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 토스 페이먼츠 API 에러 응답 DTO입니다.
 * <p>
 * 결제 승인, 취소 등 토스 API 호출 실패 시 반환되는
 * 표준 에러 포맷(code, message)을 매핑합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Getter
@NoArgsConstructor
public class TossErrorDto {
	private String code;
	private String message;
}