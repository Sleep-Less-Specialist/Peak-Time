package com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity;
/**
 * Outbox 이벤트의 처리 상태를 나타냅니다.
 *
 * <p>
 * 비동기 워커는 {@code PENDING} 상태의 이벤트만 처리하며,
 * 처리 결과에 따라 {@code DONE} 또는 {@code FAILED} 상태로 전이됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 10.
 */
public enum OutboxStatus {

    PENDING,
    DONE,
    FAILED
}