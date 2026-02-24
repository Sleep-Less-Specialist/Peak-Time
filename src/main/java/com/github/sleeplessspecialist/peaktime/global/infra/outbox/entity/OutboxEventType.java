package com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity;

/**
 * Outbox에서 처리되는 이벤트 유형을 정의합니다.
 *
 * <p>
 * Outbox 워커는 이벤트 유형({@code OutboxEventType})을 기준으로
 * 후처리 핸들러를 라우팅하며, 각 이벤트는 명확한 비즈니스 의미를 가집니다.
 * </p>
 *
 * @author 주우재
 * @version 1.1
 * @since 2026. 2. 13.
 */
public enum OutboxEventType {

    PAYMENT_CONFIRMED_ENROLLMENT,
    PAYMENT_CONFIRMED_NOTICE,
    PAYMENT_CONFIRMED_RANKING,

    PAYMENT_CANCELED_ENROLLMENT,
    PAYMENT_CANCELED_NOTICE
}

