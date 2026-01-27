package com.github.sleeplessspecialist.peaktime.domain.order.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * .
 * 주문(Order) 관련 HTTP API를 제공하는 컨트롤러
 *
 * <p>
 * 주문 생성 요청을 받아 {@link OrderService}에 위임하고,
 * 생성 결과를 공통 응답 포맷({@link ApiResponse})으로 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@RestController
@RequestMapping("/api/v1/orders")
@RequiredArgsConstructor
public class OrderController {

	private final OrderService orderService;

	@PostMapping
	public ApiResponse<CreateOrderRes> createOrder(@RequestBody @Valid CreateOrderReq request) {

		Long userId = 1L;
		CreateOrderRes response = orderService.createOrder(userId, request);
		return ApiResponse.of(SuccessCode.CREATED, response);
	}
}
