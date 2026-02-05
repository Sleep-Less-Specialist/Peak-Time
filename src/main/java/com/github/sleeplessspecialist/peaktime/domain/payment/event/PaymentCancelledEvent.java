package com.github.sleeplessspecialist.peaktime.domain.payment.event;

import lombok.Builder;
import lombok.Getter;

/**
 * 결제 취소가 성공적으로 처리된 이후,
 * 커밋 완료(트랜잭션 AFTER_COMMIT) 시점에 비동기 후처리를 트리거하기 위한 이벤트.
 *
 * <p>
 * Toss 결제 취소 결과를 기반으로, 수강(enrollment) 상태 전이 등
 * 외부 시스템/후속 도메인 작업을 비동기로 수행할 때 사용한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 31.
 */
@Getter
@Builder
public class PaymentCancelledEvent {

	private final Long orderId;
	private final Long userId;

}
