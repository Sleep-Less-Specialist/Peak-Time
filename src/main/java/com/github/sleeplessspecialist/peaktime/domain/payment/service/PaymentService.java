package com.github.sleeplessspecialist.peaktime.domain.payment.service;

import java.math.BigDecimal;

import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.exception.OrderErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.PaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.utill.OrderIdParser;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.payment.TossPaymentClient;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 결제 도메인의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * 결제 승인 요청 시 트랜잭션을 관리하며, 외부 결제 클라이언트(TossPaymentClient)를 호출하여
 * 실제 결제 승인을 수행하고 결과를 반환합니다. 추후 주문 상태 업데이트 로직이 포함됩니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

	private final OrderRepository orderRepository;
	private final TossPaymentClient tossPaymentClient;
	private final PointService pointService;
	private final ApplicationEventPublisher eventPublisher;

	@Transactional
	public void confirmPayment(TossPaymentConfirmReq req) {

		TossPaymentConfirmRes result = tossPaymentClient.confirm(req); // 1) toss 결제 승인 (동기)

		Long  orderId = Long.valueOf(OrderIdParser.extractOrderId(result.getOrderId()));
		Order order = getOrder(orderId);
		Long usePoint = order.getUsePoint().longValueExact();
		BigDecimal finalAmount = result.getTotalAmount();

		pointService.spendForOrder(order.getUser(), orderId, usePoint); // 2) 포인트 차감 (동기)

		// 3) 비동기 작업 트리거 (커밋 이후 실행되도록 리스너에서 AFTER_COMMIT 사용)
		eventPublisher.publishEvent(PaymentConfirmedEvent.builder()
			.orderId(order.getId())
			.userId(order.getUser().getId())
			.paymentKey(result.getPaymentKey())
			.finalAmount(finalAmount)
			.method(result.getMethod())
			.build());
	}

	@Transactional
	public void cancel(PaymentCancelReq req)  {

		TossPaymentCancelReq tossPaymentCancelReq = TossPaymentCancelReq.builder()
				.cancelReason(req.getCancelReason())
				.build();

		// 1) toss 결제 취소(동기)
		TossPaymentCancelRes result = tossPaymentClient.cancel(req.getPaymentKey(), tossPaymentCancelReq);

		Long orderId = Long.valueOf(OrderIdParser.extractOrderId(result.getOrderId()));
		Order order = getOrder(orderId);
		String cancelReason = result.getCancelReason();
		Long refundPoint = order.getUsePoint().longValueExact();

		pointService.refundForOrder(order.getUser(), orderId, refundPoint); // 2) 포인트 복구 (동기)

		// 3) 비동기 작업 트리거 (커밋 이후 실행되도록 리스너에서 AFTER_COMMIT 사용)

	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
	}
}