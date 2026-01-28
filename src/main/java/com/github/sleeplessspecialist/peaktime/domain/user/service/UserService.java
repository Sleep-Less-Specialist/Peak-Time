package com.github.sleeplessspecialist.peaktime.domain.user.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserProfileRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.UserUpdateReq;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.security.util.SecurityUtil;

import lombok.RequiredArgsConstructor;

/**
 * UserService 클래스입니다.
 * <p>
 * 사용자(User) 도메인의 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * 현재 로그인한 사용자의 정보 조회 및 수정 기능을 담당합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Service
@RequiredArgsConstructor

public class UserService {

	private final UserRepository userRepository;

	/**
	 * 내 정보 조회
	 */
	@Transactional(readOnly = true)
	public UserProfileRes getMyProfile() {
		Long currentUserId = SecurityUtil.getCurrentUserId();

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		return UserProfileRes.from(user);
	}

	/**
	 * 내 정보 수정
	 */
	@Transactional
	public UserProfileRes updateMyProfile(UserUpdateReq req) {

		Long currentUserId = SecurityUtil.getCurrentUserId();

		User user = userRepository.findById(currentUserId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		// Dirty Checking (변경 감지)
		user.updateProfile(req.getName(), req.getPhoneNumber());
		return UserProfileRes.from(user);
	}
}
