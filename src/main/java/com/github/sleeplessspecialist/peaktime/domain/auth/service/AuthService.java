package com.github.sleeplessspecialist.peaktime.domain.auth.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인증(Auth) 도메인의 비즈니스 로직을 처리하는 서비스입니다.
 *
 * <p>
 * 회원가입, 로그인 등 인증 과정에서 필요한 검증/저장 로직을 담당합니다.
 * 회원가입 시 휴대폰 번호를 정규화(숫자만 저장)하고, 중복 이메일을 검증합니다.
 * 회원가입이 성공하면 회원가입 보너스 포인트를 지급하고, 포인트 변동 이력을 기록합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 24.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AuthService {

	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final PointService pointService;

	/**
	 * 회원가입을 처리합니다.
	 * <p>
	 * 이메일 중복을 사전 검증하고, 비밀번호는 해시(Bcrypt)하여 저장합니다.
	 * 휴대폰 번호는 숫자만 남기도록 정규화한 뒤 저장합니다.
	 * 동시성으로 인해 DB 유니크 제약 위반이 발생하는 경우에도 동일한 에러 코드로 변환합니다.
	 * </p>
	 *
	 * @param request 회원가입 요청 DTO
	 * @return 생성된 사용자 식별자와 기본 정보를 포함한 응답 DTO
	 * @throws CustomException 이메일 중복 또는 휴대폰 번호 정책 위반 시
	 */
	@Transactional
	public SignupRes signup(SignupReq request) {
		validateSignup(request);

		String encodedPassword = passwordEncoder.encode(request.getPassword());
		String normalizedPhoneNumber = normalizePhoneNumber(request.getPhoneNumber());

		User user = User.createForSignup(
			request.getName(),
			request.getEmail(),
			encodedPassword,
			normalizedPhoneNumber
		);

		User saved;
		try {
			saved = userRepository.save(user);
		} catch (DataIntegrityViolationException e) {
			log.debug("회원가입 저장 중 이메일 중복(유니크 제약)으로 실패했습니다. email={}", request.getEmail());
			throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}

		pointService.grantSignupBonus(saved);
		log.info("회원가입 완료 - email: {}, userId: {}", saved.getEmail(), saved.getId());
		return new SignupRes(saved.getId(), saved.getEmail(), saved.getName());
	}

	private void validateSignup(SignupReq request) {
		if (userRepository.existsByEmail(request.getEmail())) {
			throw new CustomException(AuthErrorCode.EMAIL_ALREADY_EXISTS);
		}
	}

	private String normalizePhoneNumber(String phoneNumber) {
		if (phoneNumber == null) {
			return null;
		}

		String normalized = phoneNumber.replaceAll("\\D", "");

		if (normalized.length() != 10 && normalized.length() != 11) {
			log.debug(
				"휴대폰 번호 정규화 저장에 실패했습니다. raw={}, normalized={}",
				phoneNumber,
				normalized
			);
			throw new CustomException(AuthErrorCode.INVALID_PHONE_NUMBER);
		}

		return normalized;
	}
}