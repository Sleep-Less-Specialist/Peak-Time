package com.github.sleeplessspecialist.peaktime.domain.payment.repository;

import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

/**
 * 결제(Payment) 엔티티에 대한 영속성 처리를 담당하는 Repository 인터페이스.
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
public interface PaymentRepository extends JpaRepository<Payment, Long> {

    Optional<Payment> findByOrderId(Long orderId);

    @Query("""
            select p
            from Payment p
            join p.order o
            where o.user = :user""")
    Page<Payment> findPaymentsByUser(
            @Param("user") User user,
            Pageable pageable
    );
}
