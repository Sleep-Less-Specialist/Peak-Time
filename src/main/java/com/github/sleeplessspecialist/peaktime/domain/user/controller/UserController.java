package com.github.sleeplessspecialist.peaktime.domain.user.controller;

import java.util.List;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.order.dto.OrderListRes;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderService;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.service.UserService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * UserController 클래스입니다.
 * <p>
 * 사용자 본인의 정보 관리 API를 제공하는 컨트롤러 클래스입니다.
 * 내 정보 조회, 내 정보 수정, 내 구매 내역 조회
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class UserController {

	private final UserService userService;
	private final OrderService orderService;

	@GetMapping
	public ApiResponse<UserProfileRes> getMyProfile() {

		UserProfileRes response = userService.getMyProfile();
		return ApiResponse.ok(response);
	}

	@PatchMapping
	public ApiResponse<UserProfileRes> updateMyProfile(@RequestBody UserUpdateReq req) {

		UserProfileRes response = userService.updateMyProfile(req);
		return ApiResponse.ok(response);
	}

	@GetMapping("/orders")
	public ApiResponse<List<OrderListRes>> getMyOrderList() {

		List<OrderListRes> response = orderService.getMyOrderList();
		return ApiResponse.ok(response);
	}
}
