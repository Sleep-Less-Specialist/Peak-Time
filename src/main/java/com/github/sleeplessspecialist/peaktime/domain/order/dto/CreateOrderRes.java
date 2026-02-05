package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성 완료 후 클라이언트에게 반환되는 응답 DTO
 * <p>
 * 생성된 주문 항목의 기본 정보를 포함한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateOrderRes {

	private final List<OrderItemRes> items;
	private final Long orderId;
	private final Long userId;
	private final BigDecimal totalAmount;
	private final BigDecimal usePoint;
	private final LocalDateTime createdAt;
}
