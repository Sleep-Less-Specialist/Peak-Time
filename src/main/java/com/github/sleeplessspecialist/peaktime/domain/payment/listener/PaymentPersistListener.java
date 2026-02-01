package com.github.sleeplessspecialist.peaktime.domain.payment.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.service.PaymentPersistService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 완료 이벤트를 수신하여 결제 정보를 저장하는 리스너입니다.
 * <p>
 * {@link PaymentConfirmedEvent}를 트랜잭션 커밋 이후(AFTER_COMMIT)에
 * 비동기(@Async)로 처리하며,
 * 결제 영속화 로직을 {@link PaymentPersistService}에 위임한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */

@Slf4j
@Component
@RequiredArgsConstructor
public class PaymentPersistListener {

	private final PaymentPersistService paymentPersistService;

	@Async("paymentAsyncExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PaymentConfirmedEvent event) {
		log.info("[PaymentPersistListener] 실행 (orderId={})", event.getOrderId());
		paymentPersistService.createPayment(event);
	}
}