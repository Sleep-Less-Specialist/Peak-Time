package com.github.sleeplessspecialist.peaktime.domain.auth.service;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 인증 토큰 묶음(Token Bundle)입니다.
 *
 * <p>
 * {@code AuthService}가 로그인/재발급(RTR) 과정에서 생성한 토큰 정보를 컨트롤러로 전달하기 위한
 * <b>서비스 계층 내부 전달 객체</b>입니다.
 * </p>
 *
 * <p>
 * 본 클래스는 API 응답 DTO가 아닙니다.
 * 컨트롤러는 이 객체의 {@code refreshToken}을 <b>HttpOnly Cookie(Set-Cookie)</b>로 설정하고,
 * 응답 바디에는 {@code accessToken}만 노출하도록 매핑해야 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 13.
 */
@Getter
@RequiredArgsConstructor
public class TokenBundle {

	private final String accessToken;
	private final String refreshToken;
	private final String tokenType;
	private final Integer expiresIn;
}