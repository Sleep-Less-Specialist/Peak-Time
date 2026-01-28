package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 목록 조회 시 각 주문을 구성하는 단일 항목(Response Item) DTO
 * <p>
 * 주문 ID, 주문자 ID, 총 결제 금액, 사용 포인트, 주문 상태, 주문 생성 시각 정보를 포함하며,
 * 주문 목록 화면에서 각 주문의 요약 정보를 표시하는 용도로 사용됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@RequiredArgsConstructor
@Builder
public class OrderListItemRes {

	private final Long orderId;
	private final Long userId;
	private final BigDecimal totalAmount;
	private final BigDecimal usePoint;
	private final OrderStatus orderStatus;
	private final LocalDateTime createAt;

	public static OrderListItemRes from(Order order) {
		return OrderListItemRes.builder()
			.orderId(order.getId())
			.userId(order.getUser().getId())
			.totalAmount(order.getTotalAmount())
			.usePoint(order.getUsePoint())
			.orderStatus(order.getStatus())
			.createAt(order	.getCreatedAt())
			.build();
	}
}
