package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토스 페이먼츠 API로부터 결제 승인 요청에 대한 성공 응답을 수신하는 응답 DTO입니다.
 * <p>
 * 외부 API의 JSON 응답을 매핑하여 결제 상태(`status`), 결제 수단(`method`),
 * 최종 승인 금액(`totalAmount`) 등의 핵심 결제 정보를 담습니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@Builder
@RequiredArgsConstructor
public class TossPaymentConfirmReq {

	@NotBlank(message = "paymentKey는 필수입니다.")
	private final String paymentKey; // 토스가 발급한 결제 고유 ID

	@NotBlank(message = "orderId는 필수입니다.")
	private final String orderId;    // 우리 서비스에서 만든 주문 ID

	@NotNull(message = "amount는 필수입니다.")
	private final Long amount;       // 결제 금액 (KRW는 정수형 사용)
}