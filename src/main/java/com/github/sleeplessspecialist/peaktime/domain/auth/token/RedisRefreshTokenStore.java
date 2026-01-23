package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.time.Instant;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 RefreshTokenStore 구현체입니다.
 * <p>
 * RefreshToken 세션을 session:{userId}:{deviceId} 키 규칙으로 관리하며,
 * Redis Hash에 refreshTokenHash / lastActivityAt 값을 저장합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 23.
 */

@Repository
@RequiredArgsConstructor
public class RedisRefreshTokenStore implements RefreshTokenStore {

	private static final String KEY_PREFIX = "session";

	private static final String FIELD_REFRESH_TOKEN_HASH = "refreshTokenHash";
	private static final String FIELD_LAST_ACTIVITY_AT = "lastActivityAt";

	private final StringRedisTemplate redisTemplate;

	/**
	 * RefreshToken 세션을 저장합니다.
	 *
	 * @param entry 저장할 세션 엔트리
	 */
	@Override
	public void save(SessionEntry entry) {
		String key = generateKey(entry.getUserId(), entry.getDeviceId());

		redisTemplate.opsForHash().put(key, FIELD_REFRESH_TOKEN_HASH, entry.getRefreshTokenHash());
		redisTemplate.opsForHash().put(key, FIELD_LAST_ACTIVITY_AT, entry.getLastActivityAt().toString());

		redisTemplate.expire(key, entry.getTtl());
	}

	/**
	 * 사용자/디바이스에 대응하는 세션을 조회합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param deviceId 디바이스 식별자
	 * @return 조회 결과(없으면 Optional.empty())
	 */
	@Override
	public Optional<SessionEntry> find(Long userId, String deviceId) {
		String key = generateKey(userId, deviceId);

		if (notExists(key)) {
			return Optional.empty();
		}

		String refreshTokenHash = getHashValue(key, FIELD_REFRESH_TOKEN_HASH);
		String lastActivityAt = getHashValue(key, FIELD_LAST_ACTIVITY_AT);

		// 조회 시점에서는 TTL이 반드시 필요하지 않으므로 null로 반환합니다.
		return toSessionEntry(userId, deviceId, refreshTokenHash, lastActivityAt);
	}

	/**
	 * 사용자/디바이스에 대응하는 세션을 삭제합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param deviceId 디바이스 식별자
	 */
	@Override
	public void delete(Long userId, String deviceId) {
		redisTemplate.delete(generateKey(userId, deviceId));
	}

	/**
	 * 마지막 활동 시각을 갱신합니다.
	 *
	 * @param userId         사용자 식별자
	 * @param deviceId       디바이스 식별자
	 * @param lastActivityAt 마지막 활동 시각(epoch milli)
	 */
	@Override
	public void touch(Long userId, String deviceId, long lastActivityAt) {
		String key = generateKey(userId, deviceId);
		if (notExists(key)) {
			return;
		}

		redisTemplate.opsForHash().put(
			key,
			FIELD_LAST_ACTIVITY_AT,
			Instant.ofEpochMilli(lastActivityAt).toString()
		);
	}

	/**
	 * Redis 세션 키를 생성합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param deviceId 디바이스 식별자
	 * @return Redis 키(session:{userId}:{deviceId})
	 */
	private String generateKey(Long userId, String deviceId) {
		return KEY_PREFIX + ":" + userId + ":" + deviceId;
	}

	private boolean notExists(String key) {
		return Boolean.FALSE.equals(redisTemplate.hasKey(key));
	}

	private String getHashValue(String key, String field) {
		return (String)redisTemplate.opsForHash().get(key, field);
	}

	private Optional<SessionEntry> toSessionEntry(
		Long userId,
		String deviceId,
		String refreshTokenHash,
		String lastActivityAt
	) {
		if (refreshTokenHash == null || lastActivityAt == null) {
			return Optional.empty();
		}

		return Optional.of(new SessionEntry(
			userId,
			deviceId,
			refreshTokenHash,
			Instant.parse(lastActivityAt),
			null
		));
	}
}