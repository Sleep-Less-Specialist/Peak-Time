package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.util.Optional;

/**
 * RefreshToken 화이트리스트(세션) 저장소 인터페이스입니다.
 * <p>
 * AccessToken은 stateless로 처리하고, RefreshToken은 Redis에 세션 형태로 저장하여
 * 로그아웃(revoke), 무활동 만료(idle) 등 상태 기반 정책을 적용할 수 있도록 합니다.
 * </p>
 *
 * <p>
 * 키 규칙: session:{userId}:{deviceId}
 * 저장 값: refreshTokenHash, lastActivityAt, TTL
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 23.
 */
public interface RefreshTokenStore {

	/**
	 * RefreshToken 세션을 저장합니다.
	 *
	 * <p>
	 *     TTL은 RefreshToken 만료시간과 동일하게 설정하는 것을 기본 정책으로 합니다.
	 * </p>
	 * @param entry 저장할 세션 엔트리
	 */
	void save(SessionEntry entry);

	/**
	 * 사용자/디바이스에 대응하는 RefreshToken 세션을 조회합니다.
	 * @param userId 사용자 식별자
	 * @param deviceId 디바이스 식별자
	 * @return
	 */
	Optional<SessionEntry> find(Long userId, String deviceId);

	/**
	 * 사용자/디바이스에 대응하는 RefreshToken 세션을 삭제합니다.
	 * @param userId 사용자 식별자
	 * @param deviceId 디바이스 식별자
	 */
	void delete(Long userId, String deviceId);

	/**
	 * 마지막 활동 시간을 갱신합니다.
	 * <p>
	 *     무활동 만료(idle) 정책을 도입할 경우 사용합니다.
	 * </p>
	 * @param userId 사용자 식별자
	 * @param deviceId 디바이스 식별자
	 * @param lastActivityAt 갱신할 마지막 활동 시각(epoch milli)
	 */
	void touch(Long userId, String deviceId, long lastActivityAt);


}