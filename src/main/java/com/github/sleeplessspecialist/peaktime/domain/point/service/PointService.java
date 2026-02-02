package com.github.sleeplessspecialist.peaktime.domain.point.service;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.point.entity.PointTransaction;
import com.github.sleeplessspecialist.peaktime.domain.point.repository.PointTransactionRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

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
	private final UserRepository userRepository;

	/**
	 * 회원가입 보너스 포인트를 지급하고, 이력을 기록합니다.
	 * <p>
	 * 동시성/중복 호출로 인해 동일 작업이 두 번 실행될 수 있으므로,
	 * PointTransaction의 dedupKey 유니크 제약을 통해 1회만 반영되도록 보장합니다.
	 * </p>
	 *
	 * @param user 포인트 지급 대상 사용자
	 */
	@Transactional
	public void grantSignupBonus(User user) {
		long bonus = 1000L;

		// 트랜잭션 내에서 영속(Managed) 상태의 엔티티를 확보해야 users.point 변경이 DB에 반영됩니다.
		User managedUser = userRepository.findById(user.getId())
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		try {
			long balanceBefore = managedUser.getPoint();
			long balanceAfter = balanceBefore + bonus;

			// 1) users.point 반영 (dirty checking)
			managedUser.addPoint(bonus);

			// 2) point_transactions 이력 저장 (dedupKey 유니크로 멱등 보장)
			PointTransaction tx = PointTransaction.signupBonus(managedUser, bonus, balanceAfter);
			pointTransactionRepository.save(tx);

		} catch (DataIntegrityViolationException e) {
			log.debug("회원가입 보너스 포인트 지급이 이미 완료되었습니다. userId={}", managedUser.getId());
		}
	}

	/**
	 * 결제에 사용한 포인트를 차감하고, 이력을 기록합니다.
	 * <p>
	 * 동시성/중복 호출로 인해 동일 차감이 두 번 실행될 수 있으므로,
	 * PointTransaction의 dedupKey 유니크 제약을 통해 1회만 반영되도록 보장합니다.
	 * </p>
	 *
	 * @param user     포인트 차감 대상 사용자
	 * @param orderId  결제 주문 ID (dedupKey 생성에 사용)
	 * @param usePoint 사용 포인트 (양수, 0이면 아무 작업도 하지 않음)
	 */
	@Transactional
	public void spendForOrder(User user, Long orderId, Long usePoint) {
		if (usePoint == null || usePoint <= 0) {
			return;
		}

		// 트랜잭션 내에서 영속(Managed) 상태의 엔티티를 확보해야 users.point 변경이 DB에 반영됩니다.
		User managedUser = userRepository.findById(user.getId())
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		long balanceBefore = managedUser.getPoint();
		long balanceAfter = balanceBefore - usePoint;

		PointTransaction tx = PointTransaction.paymentUsePoint(managedUser, usePoint, orderId, balanceAfter);

		try {
			// 1) users.point 반영 (dirty checking)
			managedUser.addPoint(-usePoint); // 차감 반영 (User에 usePoint 메서드가 있으면 그걸로 교체)

			// 2) point_transactions 이력 저장 (dedupKey 유니크로 멱등 보장)
			pointTransactionRepository.save(tx);

		} catch (DataIntegrityViolationException e) {
			log.debug("결제 포인트 차감이 이미 처리되었습니다. userId={}, orderId={}", managedUser.getId(), orderId);
		}
	}

	/**
	 * 결제 취소(전체 환불) 시 사용 포인트를 환급하고, 이력을 기록합니다.
	 * <p>
	 * 전체 환불만 지원하므로 orderId 기준으로 dedupKey를 구성해 1회만 반영되도록 보장합니다.
	 * (PointTransaction의 dedupKey 유니크 제약 + DataIntegrityViolationException 처리)
	 * </p>
	 *
	 * @param user        포인트 환급 대상 사용자
	 * @param orderId     결제 주문 ID (dedupKey 생성에 사용)
	 * @param refundPoint 환급 포인트 (양수, 0이면 아무 작업도 하지 않음)
	 */
	public void refundForOrder(User user, Long orderId, Long refundPoint) {
		if (refundPoint == null || refundPoint <= 0) {
			return;
		}

		long balanceBefore = user.getPoint();
		long balanceAfter = balanceBefore + refundPoint;

		PointTransaction tx = PointTransaction.paymentRefundPoint(user, refundPoint, orderId, balanceAfter);

		try {
			pointTransactionRepository.save(tx);
			user.addPoint(refundPoint); // 환급 반영
		} catch (DataIntegrityViolationException e) {
			log.debug("결제 포인트 환불이 이미 처리되었습니다. userId={}, orderId={}", user.getId(), orderId);
		}
	}

}