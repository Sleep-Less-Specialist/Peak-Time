package com.github.sleeplessspecialist.peaktime.global.common.security.policy;

/**
 * 보안 경로 정책을 관리하는 클래스입니다.
 * <p>
 * Spring Security 설정에서 공개 API(permitAll)와
 * 보호 API(authenticated)의 경계를 명확히 하기 위한 경로 정책 값을 한 곳에서 관리합니다.
 * 초기 개발 단계에서는 모든 API를 허용하도록 설정하며, 이후 보안 정책 적용 시 보호가 필요한 경로만 점진적으로 분리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
public final class SecurityPathPolicy {

    /**
     * 초기 개발 단계에서 모든 요청을 허용하기 위한 공개 API 경로 정책입니다.
     */
    public static final String[] PUBLIC_ENDPOINTS = { "/**" };

	private SecurityPathPolicy() {}
}