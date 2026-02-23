package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.time.Duration;
import java.time.Instant;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * RefreshToken 세션 엔트리(SessionEntry) 값 객체입니다.
 * <p>
 * 사용자(userId)와 세션 식별자(sid) 단위로 RefreshToken 세션 상태를 관리하며,
 * Redis 기반 RefreshTokenStore에서 저장/조회에 사용됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.1
 * @since 2026. 1. 23.
 */
@Getter
@RequiredArgsConstructor
public class SessionEntry {

	private final Long userId;
	private final String sid;
	private final String refreshTokenHash;
	private final Instant lastActivityAt;
	private final Duration ttl;

}