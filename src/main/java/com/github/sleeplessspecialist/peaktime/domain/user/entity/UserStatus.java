package com.github.sleeplessspecialist.peaktime.domain.user.entity;

/**
 * 사용자 계정 상태(User Status)를 정의하는 Enum 클래스입니다.
 * <p>
 * 계정의 현재 상태를 표현하며, 로그인 가능 여부 및 서비스 접근 제어의 기준으로 사용됩니다.
 * 인증(Authentication) 및 인가(Authorization) 단계에서 보안 정책 판단에 활용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
public enum UserStatus {

	ACTIVE,
	SUSPENDED,
	DELETED

}