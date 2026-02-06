package com.github.sleeplessspecialist.peaktime.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.PasswordResetConfirm;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.PasswordResetReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.PasswordResetService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 비밀번호 재설정 API 컨트롤러
 * <p>
 * SMTP 메일을 통해 비밀번호 재설정 링크(토큰)를 발송하고, 사용자가 전달한 토큰과 새 비밀번호로 재설정을 확정하는 엔드포인트를 제공
 * </p>
 */
@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class PasswordResetController {

	private final PasswordResetService passwordResetService;

	/**
	 * 비밀번호 재설정 요청
	 * <p>
	 * 사용자가 입력한 이메일을 기반으로 비밀번호 재설정 토큰을 생성하고, SMTP 메일로 재설정 링크를 발송
	 * </p>
	 *
	 * @param req 비밀번호 재설정 요청 DTO(이메일)
	 * @return 성공 응답
	 */
	@PostMapping("/password/reset-request")
	public ApiResponse<Void> requestResetPassword(@RequestBody @Valid PasswordResetReq req) {
		passwordResetService.requestReset(req.email());
		return ApiResponse.ok();
	}

	/**
	 * 비밀번호 재설정 확정
	 * <p>
	 * 이메일로 받은 토큰의 유효성을 검증한 뒤, 새 비밀번호로 변경을 확정
	 * </p>
	 *
	 * @param req 비밀번호 재설정 확정 요청 DTO(토큰, 새 비밀번호)
	 * @return 성공 응답
	 */
	@PostMapping("/password")
	public ApiResponse<Void> confirmResetPassword(@RequestBody @Valid PasswordResetConfirm req) {
		passwordResetService.confirmReset(req.token(), req.newPassword());
		return ApiResponse.ok();
	}

}