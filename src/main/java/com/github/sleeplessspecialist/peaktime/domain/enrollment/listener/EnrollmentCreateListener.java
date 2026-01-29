package com.github.sleeplessspecialist.peaktime.domain.enrollment.listener;

import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionPhase;
import org.springframework.transaction.event.TransactionalEventListener;

import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.service.EnrollmentService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
/**
 * .
 * <p>
 *
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

