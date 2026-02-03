package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;
import java.util.List;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.PositiveOrZero;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성을 위한 요청 DTO.
 *
 * <p>
 * 주문 항목 목록
 * 총 결제 금액
 * 사용 포인트
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateOrderReq {

	@NotNull
	@Size(min = 1)
	@Valid
	private final List<CreateOrderItemReq> orderItems;

	@NotNull
	@PositiveOrZero
	private final BigDecimal usePoint;
}
