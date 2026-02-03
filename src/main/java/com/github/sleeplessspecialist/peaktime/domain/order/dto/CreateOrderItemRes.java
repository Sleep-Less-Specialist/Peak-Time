package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성 완료 후 주문 항목 정보를 반환하는 응답 DTO
 *
 * <p>
 * 생성된 주문 항목의 식별자와 강의 정보, 가격을 포함한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateOrderItemRes {

	private final Long orderItemId;
	private final Long courseId;
	private final BigDecimal price;
}
