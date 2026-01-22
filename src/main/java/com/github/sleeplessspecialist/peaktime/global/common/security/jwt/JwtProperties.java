package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;


import org.springframework.boot.context.properties.ConfigurationProperties;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * JWT 설정값을 바인딩하기 위한 Properties 클래스입니다.
 * <p>
 * application.yml 의 `jwt.*` 설정을 주입받아 JWT 생성/검증 컴포넌트에서 공통으로 사용합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@ConfigurationProperties(prefix = "jwt")
@Getter
@RequiredArgsConstructor
public class JwtProperties {

	private final String secret;
	private final long accessTokenExpirationMs;
	private final long refreshTokenExpirationMs;

}