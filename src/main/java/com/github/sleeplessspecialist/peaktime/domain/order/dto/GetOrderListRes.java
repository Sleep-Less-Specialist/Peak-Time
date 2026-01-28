package com.github.sleeplessspecialist.peaktime.domain.order.dto;

import java.util.List;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 주문 목록 조회 API의 응답 DTO
 * <p>
 * 클라이언트에게 페이징 처리된 주문 목록과 함께 현재 페이지 정보, 페이지 크기,
 * 전체 주문 개수, 전체 페이지 수, 다음 페이지 존재 여부를 전달합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
@Getter
@RequiredArgsConstructor
@Builder
public class GetOrderListRes {

	private final List<OrderListItemRes> orders;
	private final int page;
	private final int size;
	private final long totalElements;
	private final int totalPages;
	private final boolean hasNext;
}