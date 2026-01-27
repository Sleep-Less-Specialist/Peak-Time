package com.github.sleeplessspecialist.peaktime.domain.order.entity;

import java.math.BigDecimal;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *  주문(Order)에 포함된 개별 강의 상품(Course) 정보를 나타내는 엔티티.
 *  *
 *  * <p>
 *  * 하나의 주문은 여러 개의 주문 항목(OrderItem)을 가질 수 있으며,
 *  * 각 주문 항목은 하나의 강의(Course)와 해당 시점의 결제 가격(price)을 포함한다.
 *  * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Entity
@Getter
@Table(name = "order_items")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class OrderItem extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "order_id", nullable = false)
	private Order order;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Column(name = "price", nullable = false, precision = 10, scale = 2)
	private BigDecimal price;

	@Builder
	public OrderItem(Order order, Course course, BigDecimal price) {
		this.order = order;
		this.course = course;
		this.price = price;
	}
}
