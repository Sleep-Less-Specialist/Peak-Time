package com.github.sleeplessspecialist.peaktime.domain.order.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;

/**
 *  주문(Order) 엔티티에 대한 영속성 처리를 담당하는 Repository 인터페이스.
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
public interface OrderRepository extends JpaRepository<Order, Long> {
}
