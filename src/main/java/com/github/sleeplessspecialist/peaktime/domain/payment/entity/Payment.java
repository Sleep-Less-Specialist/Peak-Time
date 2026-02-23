package com.github.sleeplessspecialist.peaktime.domain.payment.entity;

import java.math.BigDecimal;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;
import com.github.sleeplessspecialist.peaktime.domain.payment.exception.PaymentErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
/**
 * 주문에 대한 결제 정보를 관리하는 엔티티입니다.
 * <p>
 * 하나의 결제는 하나의 주문(Order)과 1:1로 매핑되며,
 * 실제 결제 금액, 결제 수단, 외부 결제 시스템의 트랜잭션 식별자와
 * 결제 상태를 함께 관리합니다.
 * <br><br>
 * 결제 상태는 {@link PaymentStatus}를 통해 관리되며,
 * 결제 완료 및 환불(부분 환불 포함)과 같은 상태 전이에 사용됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
@Entity
@Table(name = "payments")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Payment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false, unique = true)
	private Order order;

	@Column(name = "imp_uid", unique = true, length = 255)
	private String impUid;

	@Column(name = "amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal amount;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private PaymentStatus status;

	@Column(name = "payment_method", length = 100)
	private String paymentMethod;

	@Builder
	public Payment(Order order, BigDecimal amount, String impUid, PaymentStatus status, String paymentMethod) {
		this.order = order;
		this.amount = amount;
		this.impUid = impUid;
		this.status = status;
		this.paymentMethod = paymentMethod;
	}

	public void refund() {
		if (status == PaymentStatus.REFUNDED) return;
		if (status != PaymentStatus.PAID) {
			throw new CustomException(PaymentErrorCode.INVALID_ORDER_STATUS);
		}
		this.status = PaymentStatus.REFUNDED;
	}

}
