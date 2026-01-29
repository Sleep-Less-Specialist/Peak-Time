package com.github.sleeplessspecialist.peaktime.domain.point.entity;

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
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 포인트 변동 이력을 저장하는 엔티티입니다.
 * <p>
 * 사용자 보유 포인트(users.point)의 증감 내역을 기록합니다.
 * 회원가입 보너스, 결제/환불, 이벤트 지급 등 포인트 정책 변경을 추적할 수 있도록
 * amount(증감량)와 balanceAfter(반영 후 잔액)를 함께 저장합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 25.
 */
@Entity
@Table(
	name = "point_transactions",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_point_tx_dedup_key", columnNames = "dedup_key")
	}
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class PointTransaction extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false)
	private Long amount;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 30)
	private PointTransactionType type;

	@Column(name = "balance_after", nullable = false)
	private Long balanceAfter;

	@Column(length = 200)
	private String memo;

	@Column(name = "dedup_key", nullable = false, length = 100)
	private String dedupKey;

	private PointTransaction(User user, Long amount, PointTransactionType type, Long balanceAfter, String memo, String dedupKey) {
		this.user = user;
		this.amount = amount;
		this.type = type;
		this.balanceAfter = balanceAfter;
		this.memo = memo;
		this.dedupKey = dedupKey;
	}

	/**
	 * 회원가입 보너스 포인트 지급 이력을 생성합니다.
	 *
	 * @param user         포인트 변동 대상 사용자
	 * @param bonusAmount  지급할 보너스 포인트(양수)
	 * @param balanceAfter 반영 후 잔액(users.point)
	 * @return 생성된 포인트 이력 엔티티
	 */
	public static PointTransaction signupBonus(User user, Long bonusAmount, Long balanceAfter) {
		String dedupKey = "SIGNUP_BONUS:" + user.getId();
		return new PointTransaction(
			user,
			bonusAmount,
			PointTransactionType.SIGNUP_BONUS,
			balanceAfter,
			"회원가입 보너스",
			dedupKey
		);
	}

	/**
	 * 결제 주문에 따른 포인트 차감 이력을 생성한다.
	 *
	 * @param user         포인트 변동 대상 사용자
	 * @param usePoint     사용한 포인트 (양수)
	 * @param orderId      결제 주문 ID
	 * @param balanceAfter 반영 후 잔액(users.point)
	 * @return 생성된 포인트 차감 이력 엔티티
	 */
	public static PointTransaction paymentUsePoint(User user, Long usePoint, Long orderId, Long balanceAfter) {
		String dedupKey = "PAYMENT_USE_POINT:ORDER:" + orderId;
		return new PointTransaction(
			user,
			-usePoint, // 차감이므로 음수
			PointTransactionType.SPEND,
			balanceAfter,
			"결제 포인트 사용 (orderId=" + orderId + ")",
			dedupKey
		);
	}

	/**
	 * 결제 취소/환불에 따른 포인트 환급 이력을 생성한다.
	 *
	 * <p>
	 * 결제 시 사용했던 포인트를 환불(복구)할 때 사용.
	 * amount는 환급이므로 양수(+)로 기록된다.
	 * </p>
	 *
	 * @param user         포인트 변동 대상 사용자
	 * @param refundPoint  환급할 포인트 (양수)
	 * @param orderId      결제 주문 ID
	 * @param balanceAfter 반영 후 잔액(users.point)
	 * @return 생성된 포인트 환급 이력 엔티티
	 */
	public static PointTransaction paymentRefundPoint(User user, Long refundPoint, Long orderId, Long balanceAfter) {
		String dedupKey = "PAYMENT_REFUND_POINT:ORDER:" + orderId;

		return new PointTransaction(
			user,
			refundPoint, // 환급이므로 양수
			PointTransactionType.REFUND,
			balanceAfter,
			"결제 포인트 환불 (orderId=" + orderId + ")",
			dedupKey
		);
	}

	/**
	 * 포인트 변동 이력을 생성합니다.
	 *
	 * <p>
	 * amount는 적립(+) 또는 차감(-) 모두 허용합니다.
	 * balanceAfter는 반영 후 잔액(users.point) 스냅샷을 의미합니다.
	 * dedupKey는 멱등 처리를 위한 키이며, 유니크 제약으로 중복 기록을 방지합니다.
	 * </p>
	 *
	 * @param user         포인트 변동 대상 사용자
	 * @param amount       포인트 증감량(적립: +, 차감: -)
	 * @param type         포인트 변동 타입
	 * @param balanceAfter 반영 후 잔액(users.point)
	 * @param memo         변동 사유 메모
	 * @param dedupKey     중복 처리를 위한 키
	 * @return 생성된 포인트 이력 엔티티
	 */
	public static PointTransaction of(
		User user, Long amount, PointTransactionType type, Long balanceAfter, String memo, String dedupKey
	) {
		return new PointTransaction(user, amount, type, balanceAfter, memo, dedupKey);
	}

}