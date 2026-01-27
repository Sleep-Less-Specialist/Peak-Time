package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 로그아웃 요청 DTO입니다.
 *
 * <p>
 * Redis 화이트리스트에서 삭제할 Refresh Token을 전달합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 27.
 */
@Getter
@RequiredArgsConstructor
public class LogoutReq {

	@NotBlank(message = "refreshToken은 필수입니다.")
	private final String refreshToken;

}