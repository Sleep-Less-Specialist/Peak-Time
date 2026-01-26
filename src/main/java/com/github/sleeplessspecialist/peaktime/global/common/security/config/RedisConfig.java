package com.github.sleeplessspecialist.peaktime.global.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.connection.RedisConnectionFactory;
import org.springframework.data.redis.core.StringRedisTemplate;

/**
 * Redis 설정 클래스입니다.
 * <p>
 * Spring Data Redis의 기본 자동 설정을 사용하되,
 * 로그인 시도 제한(브루트포스 방지)과 Refresh Token 화이트리스트 저장 등
 * 문자열 기반 Redis 작업을 위해 {@link StringRedisTemplate}을 빈으로 명시합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Configuration
public class RedisConfig {

	/**
	 * 문자열 기반 Redis 작업을 위한 템플릿 빈입니다.
	 * <p>
	 * Key/Value를 모두 문자열로 다루므로 MVP 단계의 토큰 화이트리스트, 카운터(시도 제한) 구현에 적합합니다.
	 * </p>
	 *
	 * @param connectionFactory Redis 연결 팩토리
	 * @return {@link StringRedisTemplate}
	 */
	@Bean
	public StringRedisTemplate stringRedisTemplate(final RedisConnectionFactory connectionFactory) {
		return new StringRedisTemplate(connectionFactory);
	}

}