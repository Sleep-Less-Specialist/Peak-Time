package com.github.sleeplessspecialist.peaktime.domain.payment.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.exception.OrderErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.PaymentStatus;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.exception.PaymentErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.payment.repository.PaymentRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 완료 이벤트를 처리하여 결제 정보를 영속화하는 리스너 구현 서비스
 * <p>
 * {@link PaymentConfirmedEvent}를 기반으로 Payment 엔티티를 생성하고,
 * 결제 상태를 완료(PAID)로 저장한다.
 *
 * 동일한 주문에 대해 중복 이벤트가 수신될 수 있으므로,
 * order_id 유니크 제약과 예외 처리(DataIntegrityViolationException)를 통해
 * 멱등성을 보장한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentPersistService {

	private final PaymentRepository paymentRepository;
	private final OrderRepository orderRepository;

	/**
	 * 결제 완료 이벤트를 기반으로 Payment 엔티티 생성후 저장
	 */
	@Transactional
	public void createPayment(PaymentConfirmedEvent event) {

		Order order = getOrder(event);

		Payment payment = Payment.builder()
			.order(order)
			.amount(event.getFinalAmount())
			.paymentMethod(event.getMethod())
			.status(PaymentStatus.PAID)
			.impUid(event.getPaymentKey())
			.build();

		try {
			paymentRepository.save(payment);
			log.info("Payment 저장 완료. orderId={}", event.getOrderId());
		} catch (DataIntegrityViolationException e) {
			log.debug("이미 Payment가 존재합니다. orderId={}", event.getOrderId());
		}
	}

	private Order getOrder(PaymentConfirmedEvent event) {
		 return orderRepository.findById(event.getOrderId())
			.orElseThrow(() -> new CustomException(PaymentErrorCode.ORDER_NOT_FOUND));
	}
}
