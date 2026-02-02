package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;


/**
 *  결제 취소 요청 DTO입니다.
 * <p>
 * 결제 키 (paymentKey) 는 필수
 * 취소 사유(cancelReason)는 필수.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 29.
 */
@Getter
@RequiredArgsConstructor
@Builder
public class PaymentCancelReq {

	@NotNull
	private final String paymentKey;

	@NotNull
	private final String cancelReason;
}
