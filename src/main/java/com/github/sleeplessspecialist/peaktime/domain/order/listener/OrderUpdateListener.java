package com.github.sleeplessspecialist.peaktime.domain.order.listener;

/**
 * 결제 완료 이벤트를 수신하여 주문 상태를 갱신하는 리스너.
 *  <p>
 *  {@link PaymentConfirmedEvent}가 트랜잭션 커밋 이후(AFTER_COMMIT)에 발행되면
 *  비동기(@Async)로 실행되며,
 *  결제가 완료된 주문의 상태를 {@code COMPLETED}로 전이시키는 책임을 가진다.
 *  </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderPaymentCommandService;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
@RequiredArgsConstructor
public class OrderUpdateListener {

	private final OrderPaymentCommandService orderPaymentCommandService;

	@Async("paymentAsyncExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PaymentConfirmedEvent event) {
		log.info("[OrderUpdateListener] 실행 (orderId={})", event.getOrderId());
		orderPaymentCommandService.markCompleted(event.getOrderId());
	}
}