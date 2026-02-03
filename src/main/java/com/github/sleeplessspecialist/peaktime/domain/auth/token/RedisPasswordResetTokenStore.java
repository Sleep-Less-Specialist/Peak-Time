package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.time.Duration;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 비밀번호 초기화 토큰 저장소 구현체입니다.
 *
 * <p>
 * 비밀번호 초기화 토큰은 {@code password-reset:{token}} 형태의 키로 Redis에 저장되며,
 * 값은 사용자 식별자(userId)입니다.
 * TTL(Time-To-Live)을 적용하여 만료된 토큰이 자동으로 제거되도록 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 3.
 */
@Repository
@RequiredArgsConstructor
public class RedisPasswordResetTokenStore implements PasswordResetTokenStore {

	private static final String KEY_PREFIX = "password-reset";

	private final StringRedisTemplate redisTemplate;

	/**
	 * 비밀번호 초기화 토큰을 Redis에 저장합니다.
	 * <p>
	 * 토큰은 사용자 식별자(userId)와 매핑되며,
	 * 지정된 TTL 이후 자동으로 만료됩니다.
	 * </p>
	 *
	 * @param token  비밀번호 초기화를 위한 토큰
	 * @param userId 토큰과 매핑될 사용자 식별자
	 * @param ttl    토큰 유효 시간
	 */
	@Override
	public void save(String token, Long userId, Duration ttl) {
		String key = generateKey(token);
		redisTemplate.opsForValue().set(key, String.valueOf(userId), ttl);
	}

	/**
	 * 비밀번호 초기화 토큰에 매핑된 사용자 식별자를 조회합니다.
	 *
	 * @param token 비밀번호 초기화 토큰
	 * @return 토큰이 유효한 경우 사용자 식별자, 만료되었거나 존재하지 않는 경우 Optional.empty()
	 */
	@Override
	public Optional<Long> findUserId(String token) {
		String key = generateKey(token);
		String value = redisTemplate.opsForValue().get(key);
		if (value == null) {
			return Optional.empty();
		}
		return Optional.of(Long.parseLong(value));
	}

	/**
	 * 비밀번호 초기화 토큰을 Redis에서 삭제합니다.
	 * <p>
	 * 비밀번호 재설정 확정 단계에서 호출되며, 토큰을 1회성으로 사용하기 위해 즉시 제거합니다.
	 * </p>
	 *
	 * @param token 비밀번호 초기화 토큰
	 */
	@Override
	public void delete(String token) {
		redisTemplate.delete(generateKey(token));
	}

	private String generateKey(String token) {
		return KEY_PREFIX + ":" + token;
	}

}