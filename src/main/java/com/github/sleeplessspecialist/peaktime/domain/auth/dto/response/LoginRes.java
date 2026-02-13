package com.github.sleeplessspecialist.peaktime.domain.auth.dto.response;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 로그인 요청에 대한 응답 DTO 클래스입니다.
 * <p>
 * 인증 성공 시 Access Token을 반환합니다.
 * Refresh Token은 HttpOnly 쿠키를 통해 전달되며, 응답 본문에는 포함되지 않습니다.
 * Access Token은 Stateless(JWT)로 검증되며 서버에 저장하지 않습니다.
 * Refresh Token은 Redis 화이트리스트에 저장되고 TTL로 만료가 관리됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Getter
@RequiredArgsConstructor
public class LoginRes {

	private final String accessToken;
	private final String tokenType;
	private final Integer expiresIn;

}