package com.github.sleeplessspecialist.peaktime.domain.order.repository;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

/**
 * 장바구니(OrderItem) 엔티티에 대한 영속성 처리를 담당하는 Repository 인터페이스.
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
public interface OrderItemRepository extends JpaRepository<OrderItem, Long> {

    /**
     * 특정 주문에 포함된 OrderItem과 강의 정보를 함께 조회합니다.
     */
    @Query("""
                select oi
                from OrderItem oi
                join fetch oi.course c
                where oi.order.id = :orderId
            """)
    List<OrderItem> findOrderItemWithCourseByOrderId(@Param("orderId") Long orderId);

    /**
     * 특정 주문에 포함된 OrderItem과 강의, 강사 정보를 함께 조회합니다.
     */
    @Query("""
                select oi
                from OrderItem oi
                join fetch oi.course c
                join fetch c.lecturer l
                where oi.order.id = :orderId
            """)
    List<OrderItem> findOrderItemsWithCourseAndLecturerByOrderId(
            @Param("orderId") Long orderId
    );

    /**
     * 특정 주문에 포함된 강의 ID 목록을 조회합니다.
     */
    @Query("select oi.course.id from OrderItem oi where oi.order.id = :orderId")
    List<Long> findCourseIdsByOrderId(@Param("orderId") Long orderId);

}
