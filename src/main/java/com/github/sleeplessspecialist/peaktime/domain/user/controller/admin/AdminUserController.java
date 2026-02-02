package com.github.sleeplessspecialist.peaktime.domain.user.controller.admin;

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.request.AdminUpdateUserStatusReq;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response.AdminUpdateUserStatusRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response.AdminUserListRes;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserStatus;
import com.github.sleeplessspecialist.peaktime.domain.user.service.admin.AdminUserService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;

/**
 * 관리자 사용자(User) 관리 API 컨트롤러입니다.
 * <p>
 * 관리자 권한(ROLE_ADMIN)을 가진 사용자만 접근할 수 있으며,
 * 사용자 목록 조회 등 관리자 전용 기능의 진입점 역할을 수행합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
@RestController
@RequestMapping("/api/v1/admin")
@RequiredArgsConstructor
public class AdminUserController {

	private final AdminUserService adminUserService;

	/**
	 * 사용자 목록을 Page 기반으로 조회합니다. (관리자 권한)
	 *
	 * <p>
	 * {@code page}/{@code size} 기반 페이징을 제공하며, 이메일 또는 이름 키워드 검색({@code q}),
	 * 사용자 상태({@code status}) 및 역할({@code role}) 필터링을 지원합니다.
	 * </p>
	 *
	 * @param page 조회 페이지 수
	 * @param size 조회 크기
	 * @return 조회 성공 응답
	 */
	@GetMapping("/users")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminUserListRes> getUsers(

		@RequestParam(defaultValue = "0") @Min(0) int page,
		@RequestParam(defaultValue = "20") @Min(1) @Max(100) int size
	) {
		return ApiResponse.of(
			SuccessCode.OK,
			adminUserService.getUsers(page, size)
		);
	}

	/**
	 * 특정 사용자의 상태(status)를 변경합니다. (관리자 권한)
	 *
	 * <p>
	 * 변경 가능한 상태는 ACTIVE, SUSPENDED, DELETED 입니다.
	 * DELETED 는 소프트 삭제(탈퇴 처리)로 간주합니다.
	 * </p>
	 *
	 * @param userId 대상 사용자 ID
	 * @param request 변경 요청 바디(status, reason)
	 * @return 상태 변경 성공 응답
	 */
	@PatchMapping("/users/{userId}/status")
	@PreAuthorize("hasRole('ADMIN')")
	public ApiResponse<AdminUpdateUserStatusRes> updateUserStatus(
		@PathVariable Long userId,
		@Valid @RequestBody AdminUpdateUserStatusReq request
	) {
		UserStatus status = adminUserService.updateUserStatus(userId, request);
		return ApiResponse.of(
			SuccessCode.OK,
			new AdminUpdateUserStatusRes(status)
		);
	}
}