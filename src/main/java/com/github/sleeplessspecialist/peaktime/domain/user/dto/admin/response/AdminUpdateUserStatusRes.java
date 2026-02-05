package com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserStatus;

/**
 * 관리자 사용자 상태 변경 응답 DTO
 *
 * <p>
 * 관리자에 의해 사용자 상태가 변경된 후,
 * 현재 사용자 상태를 클라이언트에 반환하기 위한 응답 객체입니다.
 * </p>
 *
 * @param status 변경된 사용자 상태
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
public record AdminUpdateUserStatusRes(UserStatus status) {

	public static AdminUpdateUserStatusRes from(UserStatus status) {
		return new AdminUpdateUserStatusRes(status);
	}
}
