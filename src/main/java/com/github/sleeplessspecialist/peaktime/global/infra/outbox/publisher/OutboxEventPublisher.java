package com.github.sleeplessspecialist.peaktime.global.infra.outbox.publisher;

import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
/**
 * OutboxPublisher 구현체입니다.
 *
 * <p>
 * 도메인 서비스에서 발행한 {@link OutboxEvent}를
 * 동일 트랜잭션 내에서 데이터베이스에 저장합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 02. 13.
 */
@Component
@RequiredArgsConstructor
public class OutboxEventPublisher implements OutboxPublisher {

    private final OutboxRepository outboxRepository;

    @Transactional
    @Override
    public void publish(OutboxEvent event) {
        outboxRepository.save(event);
    }

    @Transactional
    @Override
    public void publishAll(List<OutboxEvent> events) {
        outboxRepository.saveAll(events);
    }
}

