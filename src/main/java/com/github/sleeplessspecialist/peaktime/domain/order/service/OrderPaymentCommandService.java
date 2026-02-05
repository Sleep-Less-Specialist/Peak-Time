package com.github.sleeplessspecialist.peaktime.domain.order.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.exception.OrderErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 완료 이후 주문 상태 전이를 담당하는 커맨드 서비스.
 * <p>
 * PaymentConfirmedEvent 리스너에서 호출되며,
 * 주문 상태를 결제 완료 상태로 전이시키는 책임만을 가진다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderPaymentCommandService {

	private final OrderRepository orderRepository;

	/**
	 * 결제 완료 후처리 로직으로 주문 상태 전이
	 */
	@Transactional
	public void markCompleted(Long orderId) {
		Order order = getOrder(orderId);
		order.complete();
	}

	/**
	 * 결제 취소 후처리 로직으로 주문 상태 전이
	 */
	@Transactional
	public void markCanceled(Long orderId) {
		Order order = getOrder(orderId);
		order.cancel();
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
	}
}