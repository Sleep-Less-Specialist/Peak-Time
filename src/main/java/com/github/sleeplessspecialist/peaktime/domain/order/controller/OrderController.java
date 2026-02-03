package com.github.sleeplessspecialist.peaktime.domain.order.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderListRes;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 주문(Order) 관련 HTTP API를 제공하는 컨트롤러
 *
 * <p>
 * 주문 생성 요청을 받아 {@link OrderService}에 위임하고,
 * 생성 결과를 공통 응답 포맷({@link ApiResponse})으로 반환한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/orders")
public class OrderController {

	private final OrderService orderService;

	/**
	 * 주문 생성
	 */
	@PostMapping
	public ApiResponse<CreateOrderRes> createOrder(
		@AuthenticationPrincipal Long userId,
		@RequestBody @Valid CreateOrderReq request) {

		CreateOrderRes response = orderService.createOrder(userId, request);
		return ApiResponse.of(SuccessCode.CREATED, response);
	}

	/**
	 * 내 주문 상세 조회
	 */
	@GetMapping("/{orderId}")
	public ApiResponse<GetOrderDetailRes> getOrderDetail(
		@AuthenticationPrincipal Long userId,
		@PathVariable @Positive Long orderId) {

		GetOrderDetailRes response = orderService.getOrderDetail(userId, orderId);
		return ApiResponse.of(SuccessCode.OK, response);
	}

	/**
	 * 내 주문 전체 조회
	 */
	@GetMapping
	public ApiResponse<GetOrderListRes> getAllOrder(
		@AuthenticationPrincipal Long userId,
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size
	) {
		return ApiResponse.of(SuccessCode.OK, orderService.getAllOrder(userId, page, size));
	}
}

