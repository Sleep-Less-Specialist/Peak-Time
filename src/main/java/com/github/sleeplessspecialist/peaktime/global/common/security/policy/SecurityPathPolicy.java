package com.github.sleeplessspecialist.peaktime.global.common.security.policy;

/**
 * 보안 경로 정책을 관리하는 클래스입니다.
 * <p>
 * Spring Security 설정에서 공개 API(permitAll)와
 * 보호 API(authenticated)의 경계를 명확히 하기 위한 경로 정책 값을 한 곳에서 관리합니다.
 * 현재는 기능 개발 병행을 위해 전체 API를 공개(permitAll)로 두되,
 * 이후 인증/인가 적용 시 공개 경로(/api/v1/auth/** 등)와 보호 경로를 점진적으로 분리합니다.
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
	 * 현재는 기능 개발 병행을 위해 전체 API를 공개 처리합니다. ("/**")
	 * 이후 단계에서 아래 {@link #AUTH_ENDPOINTS} 등으로 좁혀갈 예정입니다.
	 * </p>
	 */
	public static final String[] PUBLIC_ENDPOINTS = {"/**"};

	/**
	 * 인증 관련 엔드포인트 목록입니다.
	 * <p>
	 * 인가 적용 단계에서 공개 경로를 아래 목록으로 축소하는 것을 기본값으로 가정합니다.
	 * </p>
	 */
	public static final String[] AUTH_ENDPOINTS = {
		"/api/v1/auth/login",
		"/api/v1/auth/signup",
		"/api/v1/auth/**"
	};

	private SecurityPathPolicy() {
	}
}