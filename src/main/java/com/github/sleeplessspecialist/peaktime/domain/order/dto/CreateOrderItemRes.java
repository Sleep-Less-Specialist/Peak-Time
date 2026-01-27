package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성 완료 후 클라이언트에게 반환되는 응답 DTO
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateOrderItemRes {

	private final Long orderItemId;
	private final Long courseId;
	private final BigDecimal price;
}
