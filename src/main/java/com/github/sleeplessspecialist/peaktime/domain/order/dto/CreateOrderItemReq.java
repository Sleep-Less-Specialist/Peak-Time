package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 주문 생성 시 장바구니에 담긴 단일 강의 정보를 전달하기 위한 요청 DTO
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@AllArgsConstructor
public class CreateOrderItemReq {

	@NotNull @Positive
	private Long courseId ;
}
