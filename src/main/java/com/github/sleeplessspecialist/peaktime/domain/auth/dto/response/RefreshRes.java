package com.github.sleeplessspecialist.peaktime.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토큰 재발급 응답 DTO 클래스입니다.
 * <p>
 * 유효한 Refresh Token 검증 및 화이트리스트 확인이 완료되면,
 * 새로 발급된 Access Token을 반환합니다.
 * Refresh Token은 HttpOnly Cookie를 통해 전달됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Getter
@RequiredArgsConstructor
public class RefreshRes {

	private final String accessToken;
	private final String tokenType;
	private final Integer expiresIn;

}