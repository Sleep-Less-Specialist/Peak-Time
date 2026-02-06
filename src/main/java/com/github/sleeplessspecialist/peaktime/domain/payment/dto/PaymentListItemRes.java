package com.github.sleeplessspecialist.peaktime.domain.payment.dto;

import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.PaymentStatus;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

/**
 * 결제 목록 조회 시 사용되는 아이템 DTO입니다.
 *
 * <p>
 * 결제의 핵심 정보만을 노출하여 목록 화면 구성에 사용합니다.
 * </p>
 *
 * @author 주우재
 * @since 2026.02.05
 */
@Getter
@RequiredArgsConstructor
@Builder
public class PaymentListItemRes {

    private final Long paymentId;
    private final Long orderId;
    private final BigDecimal amount;
    private final String paymentMethod;
    private final PaymentStatus status;
    private final String paymentKey;
    private final LocalDateTime createdAt;

    public static PaymentListItemRes from(Payment payment) {
        return PaymentListItemRes.builder()
                .paymentId(payment.getId())
                .orderId(payment.getOrder().getId())
                .amount(payment.getAmount())
                .paymentMethod(payment.getPaymentMethod())
                .status(payment.getStatus())
                .paymentKey(payment.getImpUid())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
