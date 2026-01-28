package com.github.sleeplessspecialist.peaktime.global.common.security.util;

import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserRole;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

/**
 * SecurityUtil 클래스입니다.
 * <p>
 * Spring Security ContextHolder에 저장된 인증 객체로부터
 * 현재 로그인한 사용자의 ID 및 권한(Role) 정보를 추출하는 유틸리티 클래스입니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
public class SecurityUtil {

	private SecurityUtil() {
	}

	/**
	 * 현재 로그인한 유저 ID 가져오기
	 */
	public static Long getCurrentUserId() {

		Authentication authentication = getAuthentication();
		Object principal = authentication.getPrincipal();

		if (principal instanceof Long) {
			return (Long)principal;
		}

		// 혹시 String으로 들어갔을 경우를 대비
		if (principal instanceof String) {
			try {
				return Long.parseLong((String)principal);
			} catch (NumberFormatException e) {
				throw new CustomException(GlobalErrorCode.INVALID_AUTHENTICATION_TYPE);
			}
		}

		throw new CustomException(GlobalErrorCode.INVALID_AUTHENTICATION_TYPE);
	}

	/**
	 * 현재 로그인한 유저의 Role 가져오기
	 */
	public static UserRole getCurrentUserRole() {

		Authentication authentication = getAuthentication();

		for (GrantedAuthority authority : authentication.getAuthorities()) {
			String roleName = authority.getAuthority();

			if (roleName.startsWith("ROLE_")) {
				String realRole = roleName.substring(5);
				try {
					return UserRole.valueOf(realRole);
				} catch (IllegalArgumentException e) {
					continue;
				}
			}
		}

		throw new CustomException(GlobalErrorCode.ROLE_NOT_FOUND);
	}

	/**
	 * 편의 메서드: 현재 유저가 강사인지 확인
	 */
	public static boolean isLecturer() {

		return getCurrentUserRole() == UserRole.LECTURER;
	}

	/**
	 * 내부 헬퍼 메서드
	 */
	private static Authentication getAuthentication() {
		final Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

		if (authentication == null || authentication.getPrincipal() == null) {
			throw new CustomException(GlobalErrorCode.LOGIN_REQUIRED);
		}
		return authentication;
	}
}