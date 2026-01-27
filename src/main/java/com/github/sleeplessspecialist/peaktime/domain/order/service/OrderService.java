package com.github.sleeplessspecialist.peaktime.domain.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderItemReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderItemRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.exception.OrderErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 주문 로직
 * <p>
 * 주문 생성, 전체 조회, 단건 주문 조회 내부 로직
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.27
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class OrderService {

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;

	@Transactional
	public CreateOrderRes createOrder(Long userId, CreateOrderReq request) {

		User user = getUser(userId);

		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal usePoint = request.getUsePoint();
		BigDecimal userPoint = BigDecimal.valueOf(user.getPoint());

		if (userPoint.compareTo(usePoint) < 0) {
			log.warn("보유 포인트 부족 : user_id={}, 보유 포인트={}, 사용 포인트={}",
				userId, userPoint, usePoint);
			throw new CustomException(OrderErrorCode.INSUFFICIENT_POINT);
		}

		Order order = Order.builder()
			.user(user)
			.totalAmount(totalAmount)
			.usePoint(request.getUsePoint())
			.build();

		orderRepository.save(order);

		List<CreateOrderItemRes> orderItems = new ArrayList<>();

		for (CreateOrderItemReq orderItem : request.getOrderItems()) {

			Course course = getCourse(orderItem.getCourseId());
			BigDecimal coursePrice = course.getPrice();
			totalAmount = totalAmount.add(coursePrice);

			OrderItem saved = OrderItem.builder()
				.order(order)
				.course(course)
				.price(course.getPrice())
				.build();
			orderItemRepository.save(saved);

			orderItems.add(CreateOrderItemRes.builder()
				.orderItemId(saved.getId())
				.courseId(course.getId())
				.price(course.getPrice())
				.build());
		}

		order.updateTotalAmount(totalAmount);

		return CreateOrderRes.builder()
			.items(orderItems)
			.orderId(order.getId())
			.userId(userId)
			.totalAmount(totalAmount)
			.usePoint(request.getUsePoint())
			.createdAt(order.getCreatedAt())
			.build();
	}

	/**
	 * User 가 DB 에 존재 하는지 검증 + 없다면 throw
	 */
	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.USER_NOT_FOUND));
	}

	/**
	 * Course 가 DB 에 존재 하는지 검증 + 없다면 throw
	 */
	private Course getCourse(Long courseId) {
		return courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.COURSE_NOT_FOUND));
	}
}