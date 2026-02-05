package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;

/**
 * OrderListRes 클래스입니다.
 * <p>
 * 내 구매 내역 조회 시 클라이언트에게 반환되는 응답 DTO입니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 2.
 */
@Getter
@Builder
public class OrderListRes {

	private Long orderId;
	private BigDecimal totalAmount;
	private BigDecimal usePoint;
	private OrderStatus status;
	private LocalDateTime orderedAt;
}
