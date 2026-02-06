package com.github.sleeplessspecialist.peaktime.domain.user.controller;

import java.util.List;

import org.springframework.http.MediaType;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderService;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.MyCourseRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.ProfileImageRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.service.UserService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import lombok.RequiredArgsConstructor;

/**
 * 사용자 본인의 정보 관리 API를 제공하는 컨트롤러 클래스입니다.
 * 내 정보 조회, 내 정보 수정, 내 강의 목록 조회, 프로필 이미지 업로드
 * </p>
 *
 * @author 기섭
 * @version 1.2
 * @since 2026. 1. 28.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/members/me")
public class UserController {

	private final UserService userService;
	private final OrderService orderService;

	/**
	 * 내 정보 조회
	 */
	@GetMapping
	public ApiResponse<UserProfileRes> getMyProfile(
		@AuthenticationPrincipal Long userId
	) {

		UserProfileRes response = userService.getMyProfile(userId);
		return ApiResponse.ok(response);
	}

	/**
	 * 내 정보 수정
	 */
	@PatchMapping
	public ApiResponse<UserProfileRes> updateMyProfile(
		@AuthenticationPrincipal Long userId,
		@RequestBody UserUpdateReq req
	) {

		UserProfileRes response = userService.updateMyProfile(userId, req);
		return ApiResponse.ok(response);
	}

	/**
	 * 내 강의 목록 조회
	 */
	@GetMapping("/courses")
	public ApiResponse<List<MyCourseRes>> getMyCourses(
		@AuthenticationPrincipal Long userId
	) {

		List<MyCourseRes> response = userService.getMyCourses(userId);
		return ApiResponse.ok(response);
	}

	/**
	 * 프로필 이미지 업로드
	 */
	@PostMapping(value = "/image", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
	public ApiResponse<ProfileImageRes> uploadProfileImage(
		@AuthenticationPrincipal Long userId,
		@RequestPart("file") MultipartFile file
	) {
		ProfileImageRes response = userService.uploadProfileImage(userId, file);
		return ApiResponse.ok(response);
	}
}