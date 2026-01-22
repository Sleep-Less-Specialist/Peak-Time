package com.github.sleeplessspecialist.peaktime.domain.user.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

/**
 * UserRpository 인터페이스입니다.
 * <p>
 * TODO: 인터페이스의 역할을 작성하세요.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
public interface UserRpository extends JpaRepository<User, Long> {
}
