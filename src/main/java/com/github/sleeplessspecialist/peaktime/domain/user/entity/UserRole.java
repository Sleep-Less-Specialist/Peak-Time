package com.github.sleeplessspecialist.peaktime.domain.user.entity;

/**
 * 사용자 권한(Role)을 정의하는 Enum 클래스입니다.
 * <p>
 * 인증(Authentication) 이후 인가(Authorization) 단계에서 사용되며,
 * JWT Access Token의 claim 값 및 Spring Security 권한 판단의 기준이 됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
public enum UserRole {

	STUDENT,
	LECTURER,
	ADMIN

}