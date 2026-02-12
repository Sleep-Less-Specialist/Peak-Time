package com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
/**
 * Outbox 패턴에서 사용되는 이벤트 엔티티입니다.
 *
 * <p>
 * 트랜잭션 내부에서 발생한 도메인 이벤트를 DB에 안전하게 적재한 뒤,
 * 비동기 워커가 이를 폴링하여 후처리 로직을 실행하기 위한 용도로 사용됩니다.
 * </p>
 *
 * <p>
 * 이벤트는 {@code PENDING} 상태로 생성되며,
 * 처리 성공 시 {@code DONE}, 재시도 한계를 초과하면 {@code FAILED} 상태로 전이됩니다.
 * 또한 {@code nextRunAt} 필드를 통해 재시도 시점을 제어합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 10.
 */
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Entity
@Table(name = "outbox_events")
public class OutboxEvent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 50)
    private OutboxEventType eventType;

    @Column(nullable = false)
    private Long aggregateId;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private OutboxStatus status = OutboxStatus.PENDING;

    @Column(nullable = false)
    private int retryCount;

    @Column(length = 1000)
    private String lastErrorMessage;

    private LocalDateTime nextRunAt;

    private LocalDateTime completedAt;

    public boolean isDone() {
        return this.status == OutboxStatus.DONE;
    }

    public boolean isFailed() {
        return this.status == OutboxStatus.FAILED;
    }

    public boolean isRunnable(LocalDateTime now) {
        return this.status == OutboxStatus.PENDING && !this.nextRunAt.isAfter(now);
    }

    /**
     * 상태 전이 메서드
     */
    public void markDone(LocalDateTime now) {
        this.status = OutboxStatus.DONE;
        this.completedAt = now;
    }

    /**
     * 상태 전이 메서드
     */
    public void markFailed(Exception ex) {
        this.status = OutboxStatus.FAILED;
        this.lastErrorMessage = safeMessage(ex);
        this.completedAt = null;
        this.nextRunAt = null;
    }

    /**
     * 상태 전이 메서드 + Backoff 로 엔티티 업데이트
     */
    public void markPendingWithBackoff(
            int nextRetryCount,
            LocalDateTime nextRunAt
    ) {
        this.status = OutboxStatus.PENDING;
        this.retryCount = nextRetryCount;
        this.nextRunAt = nextRunAt;
        this.completedAt = null;
    }

    public static OutboxEvent pending(
            OutboxEventType eventType,
            Long aggregateId
    ) {
        OutboxEvent e = new OutboxEvent();
        e.eventType = eventType;
        e.aggregateId = aggregateId;
        e.status = OutboxStatus.PENDING;
        e.retryCount = 0;
        e.nextRunAt = LocalDateTime.now();
        return e;
    }

    private static String safeMessage(Exception ex) {
        if (ex == null || ex.getMessage() == null) {
            return "Unknown error";
        }
        String message = ex.getMessage();

        if (message.length() > 1000) {
            return message.substring(0, 1000);
        }
        return message;
    }
}
