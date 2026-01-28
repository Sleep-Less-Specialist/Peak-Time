package com.github.sleeplessspecialist.peaktime.domain.user.dto;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.UserRole;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * UserProfileRes 클래스입니다.
 * <p>
 * 내 정보 조회 API 요청 시 반환되는 사용자 프로필 정보를 담는 DTO입니다.
 * 사용자 식별자, 이름, 이메일, 연락처, 프로필 이미지 URL, 포인트, 권한 정보를 포함합니다.
 * </p>
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