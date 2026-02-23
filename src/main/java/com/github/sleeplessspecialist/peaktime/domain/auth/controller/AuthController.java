package com.github.sleeplessspecialist.peaktime.domain.auth.controller;

import java.time.Duration;

import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.LoginReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.LoginRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.RefreshRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtProperties;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

import com.github.sleeplessspecialist.peaktime.domain.auth.service.TokenBundle;

/**
 * 인증 인가 관련 API 엔드포인트를 제공하는 컨트롤러입니다.
 * <p>
 * 회원가입, 로그인, 토큰 재발급, 로그아웃 API를 제공합니다.
 * </p>
 *
 * @author 재원
 * @version 2.0
 * @since 2026. 1. 23.
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/api/v2/auth")
public class AuthController {

	private final AuthService authService;
	private final JwtProperties jwtProperties;

	private void setRefreshTokenCookie(HttpServletRequest request, HttpServletResponse response, String refreshToken) {
		ResponseCookie cookie = ResponseCookie.from("refreshToken", refreshToken)
			.httpOnly(true)
			.secure(request.isSecure())
			.sameSite("Lax")
			.path("/")
			.maxAge(Duration.ofMillis(jwtProperties.getRefreshTokenExpirationMs()))
			.build();
		response.addHeader(HttpHeaders.SET_COOKIE, cookie.toString());
	}

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
	 * <p>
	 * refreshToken은 HttpOnly 쿠키로 전달되며, 응답 본문에는 포함되지 않습니다.
	 * </p>
	 *
	 * @param request 로그인 요청 정보 (이메일, 비밀번호)
	 * @param httpRequest HTTP 요청 객체 (쿠키 설정용)
	 * @param httpResponse HTTP 응답 객체 (Set-Cookie 헤더 설정용)
	 * @return JWT 토큰 정보 응답 (refreshToken은 쿠키로만 전달)
	 */
	@PostMapping("/login")
	public ApiResponse<LoginRes> login(
		@Valid @RequestBody LoginReq request,
		HttpServletRequest httpRequest,
		HttpServletResponse httpResponse
	) {
		final TokenBundle bundle = authService.login(request);
		setRefreshTokenCookie(httpRequest, httpResponse, bundle.getRefreshToken());
		return ApiResponse.ok(new LoginRes(bundle.getAccessToken(), bundle.getTokenType(), bundle.getExpiresIn()));
	}

	/**
	 * HttpOnly Cookie로 전달된 Refresh Token을 이용해 Access Token을 재발급합니다.
	 * <p>
	 * Refresh Token의 rotation과 재사용 탐지, 원자적 처리가 수행되며,
	 * 새로운 Refresh Token은 HttpOnly Cookie로 설정됩니다.
	 * 응답 본문에는 Access Token만 포함됩니다.
	 * </p>
	 *
	 * @param refreshToken HttpOnly Cookie로 전달된 Refresh Token
	 * @param httpRequest HTTP 요청 객체 (쿠키 설정용)
	 * @param response HTTP 응답 객체 (Set-Cookie 헤더 설정용)
	 * @return 새로 발급된 Access Token 정보 응답 (refreshToken은 쿠키로만 전달)
	 */
	@PostMapping("/reissue")
	public ApiResponse<RefreshRes> reissue(
		@CookieValue(name = "refreshToken", required = false) String refreshToken,
		HttpServletRequest httpRequest,
		HttpServletResponse response
	) {
		if (refreshToken == null || refreshToken.isBlank()) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		final TokenBundle bundle = authService.reissue(refreshToken);
		setRefreshTokenCookie(httpRequest, response, bundle.getRefreshToken());
		return ApiResponse.ok(new RefreshRes(bundle.getAccessToken(), bundle.getTokenType(), bundle.getExpiresIn()));
	}

	/**
	 * 로그아웃을 수행합니다.
	 *
	 * <p>
	 * HttpOnly Cookie로 전달받은 Refresh Token을 Redis 화이트리스트에서 삭제하여 이후 토큰 재발급을 차단합니다.
	 * </p>
	 *
	 * <p>
	 * Access Token은 Stateless(JWT) 특성상 서버에 저장되지 않으므로, 로그아웃 이후에도 만료 시점까지는 유효할 수 있습니다.
	 * </p>
	 *
	 * @param cookieRefreshToken 쿠키에서 전달된 Refresh Token (필수)
	 * @return 로그아웃 성공 시 204 No Content
	 */
	@PostMapping("/logout")
	public ApiResponse<Void> logout(
		@CookieValue(name = "refreshToken", required = false) String cookieRefreshToken
	) {
		if (cookieRefreshToken == null || cookieRefreshToken.isBlank()) {
			throw new CustomException(AuthErrorCode.INVALID_REFRESH_TOKEN);
		}

		authService.logout(cookieRefreshToken);
		return ApiResponse.noContent();
	}
}