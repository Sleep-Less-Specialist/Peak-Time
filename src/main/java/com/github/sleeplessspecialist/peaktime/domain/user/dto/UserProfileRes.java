package com.github.sleeplessspecialist.peaktime.domain.user.dto;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 내 정보 조회 API 요청 시 반환되는 사용자 프로필 정보를 담는 DTO입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Getter
@RequiredArgsConstructor
public class UserProfileRes {

	private final Long id;
	private final String name;
	private final String email;
	private final String phoneNumber;
	private final String profileImageUrl;
	private final long point;
	private final UserRole role;

	public static UserProfileRes from(User user) {
		return new UserProfileRes(
			user.getId(),
			user.getName(),
			user.getEmail(),
			user.getPhoneNumber(),
			user.getProfileImageUrl(),
			user.getPoint(),
			user.getRole()
		);
	}
}