package com.github.sleeplessspecialist.peaktime.domain.point.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.point.entity.PointTransaction;

/**
 * 포인트 변동 이력(PointTransaction)에 대한 영속성 처리를 담당하는 Repository입니다.
 *
 * <p>
 * 사용자 포인트 적립/차감 시 발생하는 이력을 저장하고 조회하는 역할을 수행하며, 비즈니스 로직은 포함하지 않습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 25.
 */
public interface PointTransactionRepository extends JpaRepository<PointTransaction, Long> {

}