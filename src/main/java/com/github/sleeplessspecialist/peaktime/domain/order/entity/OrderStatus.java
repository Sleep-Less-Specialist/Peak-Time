package com.github.sleeplessspecialist.peaktime.domain.order.entity;

/**
 * Order 의 도메인 상태(Status)
 * <p>
 * 결제 대기/완료/취소
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
public enum OrderStatus {

	PENDING_PAYMENT,
	COMPLETED,
	CANCELLED
}
