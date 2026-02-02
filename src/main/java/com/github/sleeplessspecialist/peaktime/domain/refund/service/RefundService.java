package com.github.sleeplessspecialist.peaktime.domain.refund.service;

import java.math.BigDecimal;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.refund.entity.Refund;
import com.github.sleeplessspecialist.peaktime.domain.refund.exception.RefundErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.refund.repository.RefundRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;

/**
 * 환불(refund) 도메인의 비즈니스 로직을 담당하는 서비스 클래스.
 * <p>
 * refund 와 payment 는 1:n 의 연관 관계지만, unique 제약조건으로 1:1 의 멱등성 보장
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Service
@RequiredArgsConstructor
public class RefundService {

	private final RefundRepository refundRepository;

	public void createRefund(Payment payment, BigDecimal cancelAmount, String cancelReason) {

		try {
			Refund refund = Refund.builder()
				.payment(payment)
				.amount(cancelAmount)
				.reason(cancelReason)
				.build();

			refundRepository.save(refund);

		} catch (DataIntegrityViolationException e) {
			throw new CustomException(RefundErrorCode.ALREADY_REFUNDED);
		}
	}
}
