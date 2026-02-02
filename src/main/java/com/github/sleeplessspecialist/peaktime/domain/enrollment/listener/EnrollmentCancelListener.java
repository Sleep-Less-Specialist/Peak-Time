package com.github.sleeplessspecialist.peaktime.domain.enrollment.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.service.EnrollmentService;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentCancelledEvent;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 취소 이벤트를 수신하여 강의 수강 상태를 취소로 전이하는 리스너.
 * <p>
 * {@link PaymentCancelledEvent}가 트랜잭션 커밋 이후(AFTER_COMMIT)에 발행되면
 * 비동기(@Async)로 실행되며,
 * 결제 취소에 따른 수강(enrollment) 상태 전이를 담당한다.
 * </p>
 *
 * <p>
 * 본 리스너는 결제 취소 트랜잭션과 분리된
 * 독립적인 트랜잭션(REQUIRES_NEW)에서 실행된다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.31
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class EnrollmentCancelListener {

	private final EnrollmentService enrollmentService;

	@Async("paymentAsyncExecutor")
	@Transactional(propagation = Propagation.REQUIRES_NEW)
	@TransactionalEventListener(phase = TransactionPhase.AFTER_COMMIT)
	public void handle(PaymentCancelledEvent event) {
		log.info("[EnrollmentCancelListener] 실행 (orderId={}", event.getOrderId());
		enrollmentService.cancelEnrollment(event.getUserId(), event.getOrderId());
	}
}
