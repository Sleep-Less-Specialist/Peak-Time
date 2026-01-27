package com.github.sleeplessspecialist.peaktime.domain.order.entity;

import java.math.BigDecimal;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
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
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 주문 엔티티
 * <p>
 * 사용자가 강의를 구매하기 위해 생성하는 주문을 표현한다.
 * 주문은 결제 대기 상태(PENDING_PAYMENT)로 생성되며,
 * 총 금액과 사용 포인트 정보를 함께 관리한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Entity
@Getter
@Table(name = "orders")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Order extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(name = "total_amount", nullable = false, precision = 10, scale = 2)
	private BigDecimal totalAmount;

	@Column(name = "use_point", nullable = false, precision = 10, scale = 2)
	private BigDecimal usePoint;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false, length = 50)
	private OrderStatus status = OrderStatus.PENDING_PAYMENT;

	@Builder
	public Order(User user, BigDecimal totalAmount, BigDecimal usePoint) {
		this.user = user;
		this.totalAmount = totalAmount;
		this.usePoint = usePoint;
	}

	public void updateTotalAmount(BigDecimal totalAmount) {
		this.totalAmount = totalAmount;
	}
}
