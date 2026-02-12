package com.github.sleeplessspecialist.peaktime.global.infra.outbox.service;

import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.exception.OutBoxErrorCode;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.repository.OutboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

/**
 * Outbox 이벤트 상태 전이(재시도/성공/실패)를 전담하는 서비스입니다.
 *
 *
 * <p>
 * 워커는 이벤트 처리 흐름만 담당하고,
 * 상태 변경(재시도 backoff, 완료 시각 기록, 실패 사유 기록 등)은
 * 본 서비스가 트랜잭션 단위로 수행합니다.
 * 이를 통해 상태 전이 정책을 중앙화하고 멀티 인스턴스 환경에서도
 * 일관된 후처리 동작을 보장합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.11
 */
@Service
@RequiredArgsConstructor
public class OutBoxService {

    private final OutboxRepository outboxRepository;

    /**
     * 이벤트를 재시도 대상으로 유지하면서, Backoff 정보 갱신 메서드
     */
    @Transactional
    public void markPendingWithBackoff(Long eventId, int  nextRetry, LocalDateTime nextRunAt) {

        OutboxEvent e = getOutboxEvent(eventId);
        e.markPendingWithBackoff(nextRetry, nextRunAt);
    }

    /**
     * 상태 전이 메서드
     */
    @Transactional
    public void markDone(Long eventId, LocalDateTime now) {

        OutboxEvent e = getOutboxEvent(eventId);
        e.markDone(now);
    }

    /**
     * 상태 전이 메서드
     */
    @Transactional
    public void markFailed(Long eventId, Exception ex) {

        OutboxEvent e = getOutboxEvent(eventId);
        e.markFailed(ex);
    }

    private OutboxEvent getOutboxEvent(Long eventId) {

        return outboxRepository.findById(eventId).orElseThrow(
                () -> new CustomException(OutBoxErrorCode.NOT_FOUND_EVENT)
        );
    }
}
