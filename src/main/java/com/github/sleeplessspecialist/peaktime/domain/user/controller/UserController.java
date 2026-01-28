package com.github.sleeplessspecialist.peaktime.domain.user.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.service.UserService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * UserController 클래스입니다.
 * <p>
 * 사용자 본인의 정보 관리 API를 제공하는 컨트롤러 클래스입니다.
 * 내 정보 조회 및 수정 기능을 제공하며, 요청 경로는 /api/v1/members/me 입니다.
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

	/**
	 * 내 정보 조회
	 */
	@GetMapping
	public ApiResponse<UserProfileRes> getMyProfile() {
		UserProfileRes response = userService.getMyProfile();
		return ApiResponse.ok(response);
	}

}
