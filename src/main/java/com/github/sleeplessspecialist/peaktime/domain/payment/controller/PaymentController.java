package com.github.sleeplessspecialist.peaktime.domain.payment.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.service.PaymentService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 프론트엔드 결제 위젯으로부터 넘어온 결제 승인 요청을 처리하는 API 컨트롤러입니다.
 * <p>
 * 클라이언트(브라우저)에서 1차 인증 후 전달받은 결제 정보(paymentKey 등)를 받아
 * 서비스 계층으로 전달하고, 최종 결제 승인 결과를 반환합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/payments")
public class PaymentController {

	private final PaymentService paymentService;

	/**
	 * 토스 페이먼츠 결제 승인 요청
	 * 프론트엔드(결제 위젯)에서 받은 paymentKey, orderId, amount로 최종 승인을 요청합니다.
	 */
	@PostMapping("/confirm")
	public ApiResponse<TossPaymentConfirmRes> confirmPayment(@RequestBody @Valid TossPaymentConfirmReq req) {
		log.info("결제 승인 요청 진입 - orderId: {}", req.getOrderId());

		paymentService.confirmPayment(req);
		return ApiResponse.ok();
	}
}