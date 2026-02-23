package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.time.Duration;
import java.time.Instant;
import java.util.Collections;
import java.util.Optional;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.script.DefaultRedisScript;
import org.springframework.data.redis.core.script.RedisScript;
import org.springframework.stereotype.Repository;

import lombok.RequiredArgsConstructor;

/**
 * Redis 기반 RefreshTokenStore 구현체입니다.
 * <p>
 * RefreshToken 세션을 session:{userId}:{sid} 키 규칙으로 관리하며,
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

	private static final RedisScript<Long> ROTATE_IF_MATCH_SCRIPT = new DefaultRedisScript<>(
	    String.format(
	        """
	        local current = redis.call('HGET', KEYS[1], '%s')
	        if not current then
	            return 0
	        end
	        if current ~= ARGV[1] then
	            return 0
	        end
	        redis.call('HSET', KEYS[1], '%s', ARGV[2])
	        redis.call('PEXPIRE', KEYS[1], ARGV[3])
	        return 1
	        """,
	        FIELD_REFRESH_TOKEN_HASH,
	        FIELD_REFRESH_TOKEN_HASH
	    ),
	    Long.class
	);

	private final StringRedisTemplate redisTemplate;

	/**
	 * RefreshToken 세션을 저장합니다.
	 *
	 * @param entry 저장할 세션 엔트리
	 */
	@Override
	public void save(SessionEntry entry) {
		String key = generateKey(entry.getUserId(), entry.getSid());

		redisTemplate.opsForHash().put(key, FIELD_REFRESH_TOKEN_HASH, entry.getRefreshTokenHash());
		redisTemplate.opsForHash().put(key, FIELD_LAST_ACTIVITY_AT, entry.getLastActivityAt().toString());

		redisTemplate.expire(key, entry.getTtl());
	}

	/**
	 * 사용자/세션(sid)에 대응하는 RefreshToken 세션을 조회합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param sid 세션 식별자
	 * @return 조회 결과(없으면 Optional.empty())
	 */
	@Override
	public Optional<SessionEntry> find(Long userId, String sid) {
		String key = generateKey(userId, sid);

		if (notExists(key)) {
			return Optional.empty();
		}

		String refreshTokenHash = getHashValue(key, FIELD_REFRESH_TOKEN_HASH);
		String lastActivityAt = getHashValue(key, FIELD_LAST_ACTIVITY_AT);

		// 조회 시점에서는 TTL이 반드시 필요하지 않으므로 null로 반환합니다.
		return toSessionEntry(userId, sid, refreshTokenHash, lastActivityAt);
	}

	/**
	 * 사용자/세션(sid)에 대응하는 RefreshToken 세션을 삭제합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param sid 세션 식별자
	 */
	@Override
	public void delete(Long userId, String sid) {
		redisTemplate.delete(generateKey(userId, sid));
	}

	/**
	 * 마지막 활동 시각을 갱신합니다.
	 *
	 * @param userId         사용자 식별자
	 * @param sid 세션 식별자
	 * @param lastActivityAt 마지막 활동 시각(epoch milli)
	 */
	@Override
	public void touch(Long userId, String sid, long lastActivityAt) {
		String key = generateKey(userId, sid);
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
	 * 저장된 Refresh Token 해시가 {@code expectedHash}와 일치하는 경우에만
	 * {@code newHash}로 교체하고 TTL을 갱신합니다. (원자적 compare-and-set 회전)
	 *
	 * <p>
	 * delete+save 방식은 동시 요청(레이스 컨디션)에서 두 요청이 모두 통과할 수 있으므로,
	 * Lua 스크립트를 사용해 Redis 내부에서 비교-교체를 단일 연산으로 수행합니다.
	 * </p>
	 *
	 * @param userId 사용자 식별자
	 * @param sid 세션 식별자
	 * @param expectedHash 기존 Refresh Token 해시
	 * @param newHash 새 Refresh Token 해시
	 * @param ttl 만료 시간
	 * @return 교체 성공 시 {@code true}, 조건 불일치/세션 없음이면 {@code false}
	 */
	@Override
	public boolean rotateIfMatch(
		Long userId,
		String sid,
		String expectedHash,
		String newHash,
		Duration ttl
	) {
		String key = generateKey(userId, sid);

		Long result = redisTemplate.execute(
			ROTATE_IF_MATCH_SCRIPT,
			Collections.singletonList(key),
			expectedHash,
			newHash,
			String.valueOf(ttl.toMillis())
		);

		return result != null && result == 1L;
	}

	/**
	 * Redis 세션 키를 생성합니다.
	 *
	 * @param userId   사용자 식별자
	 * @param sid 세션 식별자
	 * @return Redis 키(session:{userId}:{sid})
	 */
	private String generateKey(Long userId, String sid) {
		return KEY_PREFIX + ":" + userId + ":" + sid;
	}

	private boolean notExists(String key) {
		return !Boolean.TRUE.equals(redisTemplate.hasKey(key));
	}

	private String getHashValue(String key, String field) {
		return (String)redisTemplate.opsForHash().get(key, field);
	}

	private Optional<SessionEntry> toSessionEntry(
		Long userId,
		String sid,
		String refreshTokenHash,
		String lastActivityAt
	) {
		if (refreshTokenHash == null || lastActivityAt == null) {
			return Optional.empty();
		}

		return Optional.of(new SessionEntry(
			userId,
			sid,
			refreshTokenHash,
			Instant.parse(lastActivityAt),
			null
		));
	}
}