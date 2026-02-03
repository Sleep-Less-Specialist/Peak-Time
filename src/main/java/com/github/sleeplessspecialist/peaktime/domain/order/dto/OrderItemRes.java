package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.math.BigDecimal;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 생성 완료 후 클라이언트에게 반환되는 주문 항목 응답 DTO
 *
 * <p>
 * 주문에 포함된 개별 강의의 기본 정보를 포함한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Getter
@RequiredArgsConstructor
@Builder
public class OrderItemRes {

	private final Long orderItemId;
	private final Long courseId;
	private final BigDecimal price;
}
