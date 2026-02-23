package com.github.sleeplessspecialist.peaktime.global.infra.outbox.worker;

import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.exception.OutBoxErrorCode;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.handler.EventHandler;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.repository.OutboxRepository;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.service.OutBoxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.PageRequest;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.util.List;
import java.util.concurrent.Executor;

/**
 * Outbox 테이블에 적재된 이벤트를 주기적으로 조회하여
 * 후처리 로직을 실행하는 비동기 워커입니다.
 *
 * <p>
 * 스케줄러를 통해 {@code PENDING} 상태이면서 실행 가능 시각({@code nextRunAt})이
 * 도래한 이벤트만 배치 단위로 조회하며,
 * 처리 결과에 따라 이벤트 상태를 {@code DONE} 또는 {@code FAILED}로 전이시킵니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 10.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class OutboxWorker {

    private static final int BATCH_SIZE = 50;
    private static final int MAX_RETRY = 10;

    private final OutboxRepository outboxRepository;
    private final EventHandler eventHandler;
    private final OutBoxService outBoxService;
    private final Executor outboxExecutor;
    /**
     * 실행 가능한 OutBox 를 polling 하여 처리(process) 한다.
     */
    @Scheduled(fixedDelay = 10_000)
    public void pollAndProcess() {

        LocalDateTime now = LocalDateTime.now();
        List<OutboxEvent> events = outboxRepository.findRunnable(now, PageRequest.of(0, BATCH_SIZE));

        for (OutboxEvent e : events) {
            outboxExecutor.execute(() -> {
                try {
                    process(e.getId(), LocalDateTime.now());
                } catch (Exception ex) {
                    log.warn("Outbox 처리 스레드 실패. eventId={}", e.getId(), ex);
                }
            });
        }
    }

    /**
     * 이벤트 처리 메서드
     */
    protected void process(Long outboxEventId, LocalDateTime now) {

        OutboxEvent e = getOutboxEvent(outboxEventId);

        if (e == null
                || e.isDone()
                || e.isFailed()
                || !e.isRunnable(now)) {
            return;
        }

        try {
            eventHandler.handle(e); //여기서 이미 예외가 발생
            outBoxService.markDone(outboxEventId, now);
            log.info("[OutboxWorker] 이벤트 처리 성공. eventId={}", outboxEventId);
        } catch (Exception ex) {
            int nextRetry = e.getRetryCount() + 1;

            if (nextRetry >= MAX_RETRY) {
                outBoxService.markFailed(e.getId(), ex);
            } else {
                LocalDateTime nextRunAt = computeNextRunAt(nextRetry, now);

                outBoxService.markPendingWithBackoff(e.getId(), nextRetry, nextRunAt);
                log.warn(
                        "[OutboxWorker] 이벤트 재시도 예약. eventId={}, retryCount={}, nextRunAt={}",
                        outboxEventId,
                        nextRetry,
                        nextRunAt
                );
            }
        }
    }

    private LocalDateTime computeNextRunAt(int retry, LocalDateTime now) {

        long seconds = Math.min(600, 1L << Math.min(retry, 10));
        return now.plusSeconds(seconds);
    }

    private OutboxEvent getOutboxEvent(Long outboxEventId) {
        return outboxRepository.findById(outboxEventId).orElseThrow(
                () -> new CustomException(OutBoxErrorCode.NOT_FOUND_EVENT)
        );
    }
}
