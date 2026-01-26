package com.github.sleeplessspecialist.peaktime.domain.auth.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LoginReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.RefreshReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.LoginRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.RefreshRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

/**
 * 인증 인가 관련 API 엔드포인트를 제공하는 컨트롤러입니다.
 * <p>
 * 회원가입, 로그인, 로그아웃 등 기본 인증인가에 필요한 기능을 제공합니다.
 * 현재는 회원가입 기능만 포함합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 23.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v1/auth")
public class AuthController {

	private final AuthService authService;

	/**
	 * 회원가입을 처리합니다.
	 * <p>
	 * 이메일 중복 여부를 검증한 후 사용자를 생성하며,
	 * 회원가입에 따른 비즈니스 로직은 서비스 레이어에서 처리됩니다.
	 * </p>
	 *
	 * @param request 회원가입 요청 정보 (이메일, 비밀번호, 이름, 휴대폰 번호)
	 * @return 회원가입 결과 응답
	 */
	@PostMapping("/signup")
	public ApiResponse<SignupRes> signup(@Valid @RequestBody SignupReq request) {
		final SignupRes response = authService.signup(request);
		return ApiResponse.created(response);
	}

	/**
	 * 로그인을 처리합니다.
	 *
	 * @param request 로그인 요청 정보 (이메일, 비밀번호)
	 * @return JWT 토큰 정보 응답
	 */
	@PostMapping("/login")
	public ApiResponse<LoginRes> login(@Valid @RequestBody LoginReq request) {
		final LoginRes response = authService.login(request);
		return ApiResponse.ok(response);
	}

	/**
	 * Refresh Token을 기반으로 Access Token(및 Refresh Token)을 재발급합니다.
	 * <p>
	 * 클라이언트가 전달한 Refresh Token의 유효성(서명/만료)과 Redis 화이트리스트 존재 여부를 검증한 뒤,
	 * 새 토큰을 발급합니다.
	 * </p>
	 *
	 * @param request 토큰 재발급 요청 정보 (refreshToken)
	 * @return 새로 발급된 토큰 정보 응답
	 */
	@PostMapping("/refresh")
	public ApiResponse<RefreshRes> refresh(@Valid @RequestBody RefreshReq request) {
		final RefreshRes response = authService.refreshToken(request);
		return ApiResponse.ok(response);
	}
}