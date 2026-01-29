package com.github.sleeplessspecialist.peaktime.domain.order.service;

import org.springframework.stereotype.Service;

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

import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;
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
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderPaymentCommandService {

	private final OrderRepository orderRepository;

	/**
	 * 결제 완료 후처리 로직으로 주문 상태 전이
	 *
	 * <p>
	 * 이미 결제 완료된 주문일 경우 멱등성 보장
	 * </p>
	 *
	 * @param orderId 결제가 완료된 주문 ID
	 */
	public void markCompleted(Long orderId) {

		Order order = getOrder(orderId);

		validateStatus(orderId, order);

		order.updateStatus(OrderStatus.COMPLETED);
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
	}

	private void validateStatus(Long orderId, Order order) {

		if (order.getStatus() == OrderStatus.COMPLETED) {
			log.info("이미 결제 완료된 주문입니다. orderId={}", orderId);
			return;
		}

		if (order.getStatus() != OrderStatus.PENDING_PAYMENT) {
			log.warn("결제 완료 처리 불가능한 주문 상태입니다. orderId={}, status={}",
				orderId, order.getStatus());
			throw new CustomException(OrderErrorCode.INVALID_ORDER_STATUS);
		}
	}
}