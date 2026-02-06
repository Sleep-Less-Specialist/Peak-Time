package com.github.sleeplessspecialist.peaktime.domain.user.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.ProfileImage;

/**
 * 사용자의 프로필 이미지 정보를 관리하는 레포지토리입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 6.
 */
public interface ProfileImageRepository extends JpaRepository<ProfileImage, Long> {

	Optional<ProfileImage> findByUserIdAndIsPrimaryTrue(Long userId);
}
