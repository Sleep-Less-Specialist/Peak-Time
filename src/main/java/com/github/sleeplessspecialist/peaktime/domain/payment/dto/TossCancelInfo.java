package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import java.math.BigDecimal;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 토스 결제 취소 내역 DTO입니다.
 * <p>
 * cancels 배열의 요소를 매핑합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Getter
@RequiredArgsConstructor
public class TossCancelInfo {

	private final String cancelReason;

	private final BigDecimal cancelAmount;
}