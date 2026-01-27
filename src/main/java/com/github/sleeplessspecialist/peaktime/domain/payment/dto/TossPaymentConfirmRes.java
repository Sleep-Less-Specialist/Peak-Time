package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토스 페이먼츠 API로부터 수신한 결제 승인 결과를 담는 응답 DTO입니다.
 * <p>
 * 외부 API의 응답 JSON을 매핑하여 결제 상태, 결제 수단, 승인 일시 등
 * 클라이언트에게 전달할 핵심 결제 정보를 정의합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@RequiredArgsConstructor
public class TossPaymentConfirmRes {

	//필요없는 데이터는 빼고 사용하면 됩니다.
	private final String paymentKey;
	private final String orderId;
	private final String orderName;   // 주문명 (예: "토스 티셔츠 외 2건")
	private final String status;      // 결제 상태 (DONE, CANCELED, ABORTED, PARTIAL_CANCELED)
	private final String method;      // 결제 수단 (카드, 가상계좌, 간편결제)
	private final Long totalAmount;   // 결제된 총 금액
	private final String requestedAt; // 결제 요청 시각 (ISO 8601)
	private final String approvedAt;  // 결제 승인 시각 (ISO 8601)
}