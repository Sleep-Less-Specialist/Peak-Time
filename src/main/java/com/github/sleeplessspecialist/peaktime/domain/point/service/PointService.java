package com.github.sleeplessspecialist.peaktime.domain.point.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.point.entity.PointTransaction;
import com.github.sleeplessspecialist.peaktime.domain.point.repository.PointTransactionRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 포인트 적립/차감 및 이력(PointTransaction) 기록을 담당하는 서비스입니다.
 * <p>
 * 포인트 정책(회원가입 보너스 등)에 따라 users.point를 갱신하고,
 * point_transactions에 변동 이력을 저장합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 25.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class PointService {

	private final PointTransactionRepository pointTransactionRepository;

	/**
	 * 회원가입 보너스 포인트를 지급하고, 이력을 기록합니다.
	 * <p>
	 * 동시성/중복 호출로 인해 동일 작업이 두 번 실행될 수 있으므로,
	 * PointTransaction의 dedupKey 유니크 제약을 통해 1회만 반영되도록 보장합니다.
	 * </p>
	 *
	 * @param user 포인트 지급 대상 사용자
	 */
	public void grantSignupBonus(User user) {
		long bonus = 1000L;

		long balanceBefore = user.getPoint();
		long balanceAfter = balanceBefore + bonus;

		PointTransaction tx = PointTransaction.signupBonus(user, bonus, balanceAfter);

		try {
			pointTransactionRepository.save(tx);
			user.addPoint(bonus);
		} catch (DataIntegrityViolationException e) {
			log.debug("회원가입 보너스 포인트 지급이 이미 완료되었습니다. userId={}", user.getId());
		}
	}

}