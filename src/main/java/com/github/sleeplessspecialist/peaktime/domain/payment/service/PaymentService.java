package com.github.sleeplessspecialist.peaktime.domain.payment.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
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
@Transactional(readOnly = true)
public class PaymentService {

	private final TossPaymentClient tossPaymentClient;

	/**
	 * 토스 결제 승인 및 주문 상태 변경
	 */
	@Transactional
	public TossPaymentConfirmRes confirmPayment(TossPaymentConfirmReq req) {

		// 필요한 내부 로직 추가해서 사용하면 됩니다

		// 외부 API(토스)로 결제 승인 요청
		TossPaymentConfirmRes result = tossPaymentClient.confirm(req);

		log.info("결제 승인 완료 - orderId: {}, paymentKey: {}", result.getOrderId(), result.getPaymentKey());
		return result;
	}
}