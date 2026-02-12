package com.github.sleeplessspecialist.peaktime.global.infra.outbox.handler;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.service.EnrollmentService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.exception.OutBoxErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * Outbox 이벤트를 수신하여 비동기 후처리를 수행하는 핸들러입니다.
 *
 * <p>
 * 본 클래스는 Outbox 패턴 기반 아키텍처에서
 * 이벤트 타입에 따라 적절한 후처리 로직으로 라우팅하는 역할을 담당합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 11.
 */
@Service
@RequiredArgsConstructor
public class EventHandler {

    private final EnrollmentService enrollmentService;

    /**
     * OutBox 이벤트를 처리하는 메서드
     * - EventType 에 맞게 라우팅 해서 처리하는 router
     */
    @Transactional
    public void handle(OutboxEvent event) {

        switch (event.getEventType()) {
            case PAYMENT_CONFIRMED -> handlePaymentConfirmed(event);
            case PAYMENT_CANCELED  -> handlePaymentCanceled(event);
            default -> throw new CustomException(OutBoxErrorCode.UNSUPPORTED_EVENT_TYPE);
        }
    }

    /**
     * 결제 확정 이벤트에 대한 비동기 후처리를 수행 handler 메서드
     */
    public void handlePaymentConfirmed(OutboxEvent event) {

        Long aggregateId = event.getAggregateId();

        enrollmentService.createEnrollment(aggregateId);

        // notice 발송 후처리 추가

        // 실시간 인기강의 후처리 추가
    }

    /**
     * 결제 취소 이벤트에 대한 비동기 후처리를 수행 handler 메서드
     */
    public void handlePaymentCanceled(OutboxEvent event) {

        Long aggregateId = event.getAggregateId();

        enrollmentService.cancelEnrollment(aggregateId);

        // notice 발송 후처리 추가

    }
}
