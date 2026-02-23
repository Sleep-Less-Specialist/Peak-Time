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

    /**
     * 단일 Outbox 이벤트를 발행(영속화)합니다.
     *
     * <p>
     * 이벤트 저장은 현재 트랜잭션에 참여하여 수행되며,
     * 트랜잭션 커밋 이후 OutboxWorker가 해당 이벤트를 조회하여
     * 비동기 후처리를 실행합니다.
     * </p>
     *
     * <p>
     * 본 메서드는 "이벤트를 즉시 실행"하지 않으며,
     * 오직 Outbox 테이블에 기록하는 역할만 수행합니다.
     * </p>
     *
     * @param event 저장할 Outbox 이벤트
     */
    void publish(OutboxEvent event);
    /**
     * 복수 Outbox 이벤트를 일괄 발행(영속화)합니다.
     *
     * <p>
     * Fan-out 구조(하나의 도메인 이벤트 → 여러 후처리 이벤트)에서
     * 여러 개의 {@link OutboxEvent}를 한 번에 저장하기 위해 사용합니다.
     * </p>
     *
     * @param events 저장할 Outbox 이벤트 목록
     */
    void publishAll(List<OutboxEvent> events);
}
