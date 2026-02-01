package com.github.sleeplessspecialist.peaktime.domain.enrollment.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.service.EnrollmentService;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 완료 이벤트를 수신하여 강의 수강 등록 생성 리스너.
 * <p>
 * {@link PaymentConfirmedEvent}가 트랜잭션 커밋 이후(AFTER_COMMIT)에 발행되면
 *  비동기(@Async)로 실행되며,
 *  강의 수강 등록 엔티티를 생성하는 책임을 가진다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentCreateListener {

	private final EnrollmentService enrollmentService;

	@Async("paymentAsyncExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PaymentConfirmedEvent event) {
		log.info("[EnrollmentCreateListener] 실행 (orderId={})", event.getOrderId());
		enrollmentService.createEnrollment(event.getOrderId());
	}
}

