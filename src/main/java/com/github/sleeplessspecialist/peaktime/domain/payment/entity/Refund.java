package com.github.sleeplessspecialist.peaktime.domain.payment.entity;

/**
 * 결제에 대한 환불 정보를 관리하는 엔티티입니다.
 * <p>
 * 하나의 결제(Payment)에 대해 여러 건의 환불이 발생할 수 있으며,
 * 부분 환불 및 전체 환불 이력을 금액과 사유 단위로 저장합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */

import java.math.BigDecimal;

import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "refunds")
@Getter
@NoArgsConstructor
public class Refund extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "payment_id", nullable = false)
	private Payment payment;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Column(name = "reason", columnDefinition = "TEXT")
	private String reason;

	@Builder
	public Refund(Payment payment, BigDecimal amount, String reason) {
		this.payment = payment;
		this.amount = amount;
		this.reason = reason;
	}
}