package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.List;

/**
 * 내 결제 목록 조회 응답 DTO입니다.
 *
 * <p>
 * 페이지네이션 메타데이터와 결제 목록을 함께 반환합니다.
 * </p>
 *
 * @author 주우재
 * @since 2026.02.05
 */
@Getter
@RequiredArgsConstructor
@Builder
public class GetMyPaymentListRes {

    private final List<PaymentListItemRes> payments;
    private final int page;
    private final int size;
    private final long totalElements;
    private final int totalPages;
    private final boolean hasNext;
}
