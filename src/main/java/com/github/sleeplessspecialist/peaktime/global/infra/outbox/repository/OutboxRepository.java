package com.github.sleeplessspecialist.peaktime.global.infra.outbox.repository;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxStatus;

/**
 * Outbox 이벤트 조회/저장을 담당하는 Repository 입니다.
 *
 * <p>
 * 비동기 워커가 처리할 이벤트를 조회하기 위해 {@code PENDING} 상태와 실행 가능 시각({@code nextRunAt})을 기준으로
 * 실행 가능한 이벤트를 제한된 배치 크기로 가져옵니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 10.
 */
public interface OutboxRepository extends JpaRepository<OutboxEvent, Long> {

    /**
     * 현재 시각 기준으로 실행 가능한 Outbox 이벤트를 조회합니다.
     */
    @Query("""
		select e
		from OutboxEvent e
		where e.status = com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxStatus.PENDING
		  and e.nextRunAt <= :now
		order by e.id asc""")
    List<OutboxEvent> findRunnable(@Param("now") LocalDateTime now, Pageable pageable);

    /**
     * 지정한 상태의 Outbox 이벤트를 조회합니다.
     */
    @Query("""
		select e
		from OutboxEvent e
		where e.status = :status
		order by e.id desc""")
    List<OutboxEvent> findByStatus(@Param("status") OutboxStatus status, Pageable pageable);
}
