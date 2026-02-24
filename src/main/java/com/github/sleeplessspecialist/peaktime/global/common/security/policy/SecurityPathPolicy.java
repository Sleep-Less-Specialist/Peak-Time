package com.github.sleeplessspecialist.peaktime.global.common.security.policy;

/**
 * Spring Security 설정에서 사용하는 경로 정책을 정의합니다.
 *
 * <p>
 * 명세 기준으로 비로그인 허용(permitAll) 범위와
 * 역할(ROLE) 기반 보호 경로를 한 곳에서 관리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
public final class SecurityPathPolicy {

	/**
	 * 공개(permitAll) 엔드포인트 목록입니다.
	 * <p>
	 * 인증/인가(비로그인 가능)
	 * </p>
	 */
	public static final String[] PUBLIC_ENDPOINTS = {
		"/error",
		// ✅ 기본 프론트 진입
		"/",
		"/index.html",
		"/signup.html",
		"/favicon.ico",
		// ✅ 비밀번호 재설정 화면 진입(메일 링크)
		"/reset-password",
		"/reset-password.html",


		// 정적 리소스
		"/css/**",
		"/js/**",
		"/images/**",

		// 인증/인가(비로그인 가능)
		"/api/v2/auth/signup",
		"/api/v2/auth/login",
		"/api/v2/auth/reissue",
		"/api/v2/auth/logout",
		"/api/v2/auth/password/**",

		// OAuth2
		"/oauth2/**",
		"/login/oauth2/**",
		"/api/v2/oauth2/**"
	};

	/**
	 * 공개 조회(GET) 엔드포인트 목록입니다.
	 * <p>
	 * 명세에서 "공통(비로그인)"으로 정의된 조회 API만 포함합니다.
	 * (중요) POST/PATCH/DELETE까지 열리지 않도록 SecurityConfig에서 HttpMethod.GET와 함께 사용해야 합니다.
	 * </p>
	 */
	public static final String[] PUBLIC_GET_ENDPOINTS = {
		"/api/v1/courses",
		"/api/v1/courses/*",
		"/api/v1/reviews",
		"/api/v1/reviews/*",
		"/api/v1/chat/room",
        "/api/v1/courses/ranking/last-3-days"
	};

	// 관리자
	public static final String ADMIN_ENDPOINTS = "/api/v1/admin/**";

	// 강의자
	public static final String LECTURER_ENDPOINTS = "/api/v1/lecturer/**";

	private SecurityPathPolicy() {
	}
}