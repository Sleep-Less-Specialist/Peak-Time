package com.github.sleeplessspecialist.peaktime.domain.order.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

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

	List<Order> findAllByUserIdOrderByCreatedAtDesc(Long userId);
	Page<Order> findByUser(User user, Pageable pageable);
}
