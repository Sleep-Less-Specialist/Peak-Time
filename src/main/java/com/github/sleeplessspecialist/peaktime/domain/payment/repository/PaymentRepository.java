package com.github.sleeplessspecialist.peaktime.domain.payment.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;

/**
 * 결제(Payment) 엔티티에 대한 영속성 처리를 담당하는 Repository 인터페이스.
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

	Optional<Payment> findByOrderId(Long orderId);
}
