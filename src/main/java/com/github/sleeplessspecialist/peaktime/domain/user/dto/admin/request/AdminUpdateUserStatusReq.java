package com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.request;

import org.hibernate.validator.constraints.Length;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserStatus;

import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * AdminUpdateUserStatusReq 클래스입니다.
 * <p>
 * 관리자가 특정 사용자의 계정 상태를 변경할 때 사용하는 요청 바디입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class AdminUpdateUserStatusReq {

	@NotNull
	private UserStatus status;

	@Length(max = 500)
	private String reason;

}