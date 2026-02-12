package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.util.Optional;
import java.time.Duration;

/**
 * RefreshToken 화이트리스트(세션) 저장소 인터페이스입니다.
 * <p>
 * AccessToken은 stateless로 처리하고, RefreshToken은 Redis에 세션 형태로 저장하여
 * 로그아웃(revoke), 무활동 만료(idle) 등 상태 기반 정책을 적용할 수 있도록 합니다.
 * </p>
 *
 * <p>
 * 키 규칙: session:{userId}:{sid}
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


	/**
	 * 기존 Refresh Token 해시와 일치하는 경우에만 새로운 해시로 교체합니다. (원자적 회전)
	 *
	 * <p>
	 * 동시 요청/레이스 컨디션 상황에서 delete+save 방식의 비원자성을 방지하기 위해,
	 * 저장소 단에서 비교-교체(compare-and-set) 형태로 회전을 수행합니다.
	 * </p>
	 *
	 * @param userId 사용자 식별자
	 * @param sid 세션 식별자
	 * @param expectedHash 기존 Refresh Token 해시
	 * @param newHash 새 Refresh Token 해시
	 * @param ttl 만료 시간
	 * @return 교체 성공 시 true, 실패 시 false
	 */
	boolean rotateIfMatch(
		Long userId,
		String sid,
		String expectedHash,
		String newHash,
		Duration ttl
	);

}