package com.github.sleeplessspecialist.peaktime.domain.auth.token;

import java.time.Duration;
import java.util.Optional;

/**
 * 비밀번호 초기화(Password Reset) 토큰을 저장/조회/삭제하기 위한 저장소 인터페이스입니다.
 *
 * <p>
 * 비밀번호 초기화 토큰은 짧은 유효 시간(TTL)을 가지는 1회성 값으로, 요청 단계에서 생성되어 확정 단계에서 검증 및 삭제됩니다.
 * </p>
 *
 * <p>
 * 구현체는 Redis 등 TTL 기반 저장소를 사용하여 토큰 만료 및 재사용 방지를 간단하게 처리하는 것을 전제로 합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 3.
 */
public interface PasswordResetTokenStore {

	/**
	 * 비밀번호 초기화 토큰을 저장합니다.
	 *
	 * <p>
	 * 토큰은 사용자 식별자(userId)와 매핑되며, 지정된 TTL 이후 자동으로 만료됩니다.
	 * </p>
	 *
	 * @param token 비밀번호 초기화를 위한 토큰
	 * @param userId 토큰과 매핑될 사용자 식별자
	 * @param ttl 토큰 유효 시간
	 */
	void save(String token, Long userId, Duration ttl);

	/**
	 * 비밀번호 초기화 토큰에 매핑된 사용자 식별자를 조회합니다.
	 *
	 * @param token 비밀번호 초기화 토큰
	 * @return 토큰이 유효한 경우 사용자 식별자, 만료 또는 존재하지 않으면 Optional.empty()
	 */
	Optional<Long> findUserId(String token);

	/**
	 * 비밀번호 초기화 토큰을 삭제합니다.
	 *
	 * <p>
	 * 비밀번호 재설정 확정 단계에서 호출되며, 토큰을 1회성으로 사용하기 위해 즉시 제거합니다.
	 * </p>
	 *
	 * @param token 비밀번호 초기화 토큰
	 */
	void delete(String token);

}