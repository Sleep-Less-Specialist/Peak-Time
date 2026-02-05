package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 상세 조회 API 호출 시 클라이언트에게 반환되는 응답 DTO
 *
 * <p>
 * 주문 항목 목록과 결제 금액, 상태 등 주문의 상세 정보를 포함한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Getter
@RequiredArgsConstructor
@Builder
public class GetOrderDetailRes {

	private final List<OrderItemRes> items;
	private final Long orderId;
	private final Long userId;
	private final BigDecimal totalAmount;
	private final BigDecimal usePoint;
	private final OrderStatus orderStatus;
	private final LocalDateTime createTime;
	private final LocalDateTime updateTime;
}
