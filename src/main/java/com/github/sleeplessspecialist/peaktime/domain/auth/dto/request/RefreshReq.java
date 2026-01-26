package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토큰 재발급 요청 DTO 클래스입니다.
 * <p>
 * 클라이언트가 전달한 Refresh Token을 검증한 뒤 새 Access Token(및 새 Refresh Token)을 발급합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Getter
@RequiredArgsConstructor(access = AccessLevel.PROTECTED)
public class RefreshReq {

	@NotBlank
	private String refreshToken;

}