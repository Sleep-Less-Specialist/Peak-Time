package com.github.sleeplessspecialist.peaktime.domain.auth.controller;

import java.time.Duration;
import java.util.UUID;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.lang.Nullable;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LoginReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LogoutReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.RefreshReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.LoginRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.RefreshRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;

import jakarta.servlet.http.HttpServletResponse;
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
	public ApiResponse<LoginRes> login(
		@Valid @RequestBody LoginReq request,
		@CookieValue(name = "deviceId", required = false) String deviceId,
		HttpServletResponse response
	) {
		final String ensuredDeviceId = ensureDeviceId(deviceId, response);
		final LoginRes loginRes = authService.login(request, ensuredDeviceId);
		return ApiResponse.ok(loginRes);
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
	public ApiResponse<RefreshRes> refresh(
		@Valid @RequestBody RefreshReq request,
		@CookieValue(name = "deviceId", required = false) String deviceId,
		HttpServletResponse response
	) {
		final String ensuredDeviceId = ensureDeviceId(deviceId, response);
		final RefreshRes refreshRes = authService.refreshToken(request, ensuredDeviceId);
		return ApiResponse.ok(refreshRes);
	}

	/**
	 * 로그아웃을 수행합니다.
	 *
	 * <p>
	 * 클라이언트로부터 전달받은 Refresh Token을 Redis 화이트리스트에서 삭제하여 이후 토큰 재발급을 차단합니다.
	 * </p>
	 *
	 * <p>
	 * Access Token은 Stateless(JWT) 특성상 서버에 저장되지 않으므로, 로그아웃 이후에도 만료 시점까지는 유효할 수 있습니다.
	 * </p>
	 *
	 * @param deviceId 기기 식별자 쿠키 (필수)
	 * @param cookieRefreshToken 쿠키에서 전달된 Refresh Token (선택)
	 * @param request 요청 본문에서 전달된 Refresh Token (선택)
	 * @return 로그아웃 성공 시 204 No Content
	 */
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@CookieValue(name = "deviceId", required = false) String deviceId,
		@CookieValue(name = "refreshToken", required = false) String cookieRefreshToken,
		@Nullable @RequestBody(required = false) LogoutReq request
	) {
		if (deviceId == null || deviceId.isBlank()) {
			throw new CustomException(AuthErrorCode.INVALID_CREDENTIALS);
		}

		final String refreshToken = (cookieRefreshToken != null && !cookieRefreshToken.isBlank())
			? cookieRefreshToken
			: (request != null ? request.getRefreshToken() : null);

		authService.logout(refreshToken, deviceId);
		return ApiResponse.noContent();

	}

	private String ensureDeviceId(String deviceId, HttpServletResponse response) {
		if (deviceId != null && !deviceId.isBlank()) {
			return deviceId;
		}

		// 로컬/프론트 MVP: 신규 deviceId 발급 후 쿠키로 내려준다.
		String newDeviceId = UUID.randomUUID().toString().replace("-", "").substring(0, 32);

		ResponseCookie cookie = ResponseCookie.from("deviceId", newDeviceId)
			.path("/")
			.httpOnly(false)
			.secure(false) // 로컬 개발 기준. 운영에서는 true(HTTPS)
			.sameSite("Lax")
			.maxAge(Duration.ofDays(30))
			.build();

		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
		return newDeviceId;
	}
}