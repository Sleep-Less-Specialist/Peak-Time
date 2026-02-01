package com.github.sleeplessspecialist.peaktime.domain.payment.event;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 승인 완료 시 발행되는 도메인 이벤트 객체입니다.
 * <p>
 * 토스 페이먼츠 결제 승인 API 호출이 성공한 이후,
 * 주문 상태 변경, 포인트 차감, 결제 이력 저장, 알림 발송 등
 * 후속 비즈니스 로직을 비동기적으로 처리하기 위해 사용됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
@Getter
@Builder
public class PaymentConfirmedEvent {

	private final Long orderId;
	private final String paymentKey;
	private final BigDecimal finalAmount;
	private final String method;
	private final Long userId;
}
