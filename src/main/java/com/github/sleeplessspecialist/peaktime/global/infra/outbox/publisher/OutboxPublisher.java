package com.github.sleeplessspecialist.peaktime.global.infra.outbox.publisher;

import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;

import java.util.List;


/**
 * Outbox 이벤트를 영속화하기 위한 퍼블리셔 인터페이스입니다.
 *
 * <p>
 * 도메인 서비스는 직접 {@code OutboxRepository}에 접근하지 않고,
 * 본 인터페이스를 통해 이벤트를 발행함으로써
 * 이벤트 저장 책임을 추상화합니다.
 * </p>
 *
 * <p>
 * 이를 통해 도메인 로직과 Outbox 인프라 구현을 분리할 수 있으며,
 * 향후 저장 방식(DB → 메시지 브로커 등)이 변경되더라도
 * 서비스 레이어의 수정 범위를 최소화할 수 있습니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 02. 13.
 */
public interface OutboxPublisher {

    void publish(OutboxEvent event);
    void publishAll(List<OutboxEvent> events);
}
