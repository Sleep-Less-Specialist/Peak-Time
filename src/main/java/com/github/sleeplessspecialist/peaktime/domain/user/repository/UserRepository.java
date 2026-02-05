package com.github.sleeplessspecialist.peaktime.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

/**
 * 사용자(User) 엔티티의 데이터베이스 접근을 담당하는 리포지토리 인터페이스입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
public interface UserRepository extends JpaRepository<User, Long> {

	Optional<User> findByEmail(String email);

	Optional<User> findByAuthProviderAndProviderId(String authProvider, String providerId);

	boolean existsByEmail(String email);
}