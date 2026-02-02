package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import java.math.BigDecimal;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토스 결제 취소 응답 DTO (필요 필드만 매핑)
 * <p>
 * orderId 와 cancels[].cancelReason 정보만 사용합니다.
 * list[0] = cancelReason
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Getter
@RequiredArgsConstructor
@Builder
public class TossPaymentCancelRes {

	private final String orderId;
	private final List<TossCancelInfo> cancels;

	/**
	 * 첫 번째 취소 사유 편의 메서드
	 * 없다면 null 반환
	 */
	public String getCancelReason() {
		if (cancels == null || cancels.isEmpty()) {
			return null;
		}
		return cancels.get(0).getCancelReason();
	}

	public BigDecimal getCancelAmount() {
		if (cancels == null || cancels.isEmpty()) {
			return null;
		}
		return cancels.get(0).getCancelAmount();
	}
}
