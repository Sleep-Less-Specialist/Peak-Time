package com.github.sleeplessspecialist.peaktime.domain.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderItemReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderListRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.OrderItemRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.OrderListItemRes;
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

	/**
	 * 주문 생성
	 * 1. userId DB 존재 여부 확인
	 * 2. 사용 포인트 유효성 검증
	 * 3. 주문 및 장바구니 생성
	 * 4. 총 주문 금액 계산 및 반영
	 * 5. 응답 dto 생성 후 리턴
	 */
	@Transactional
	public CreateOrderRes createOrder(Long userId, CreateOrderReq request) {

		User user = getUser(userId);

		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal usePoint = request.getUsePoint();
		BigDecimal userPoint = BigDecimal.valueOf(user.getPoint());

		validateSufficientPoint(userId, userPoint, usePoint);

		Order order = Order.builder()
			.user(user)
			.totalAmount(totalAmount)
			.usePoint(request.getUsePoint())
			.build();

		orderRepository.save(order);

		List<OrderItemRes> orderItems = new ArrayList<>();

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

			orderItems.add(OrderItemRes.builder()
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
	 * 주문 상세 조회
	 * 1. userId DB 존재 여부 확인
	 * 2. orderId DB 존재 여부 확인
	 * 3. user 의 권한 검증
	 * 4. 응답 dto 생성후 리턴
	 */
	@Transactional(readOnly = true)
	public GetOrderDetailRes getOrderDetail(Long userId, Long orderId) {

		User user = getUser(userId);
		Order order = getOrder(orderId);

		validateAccess(user, order);

		List<OrderItemRes> items = new ArrayList<>();

		for (OrderItem item : orderItemRepository.findAllByOrderId(orderId)) {
			items.add(OrderItemRes.builder()
				.orderItemId(item.getId())
				.courseId(item.getCourse().getId())
				.price(item.getPrice())
				.build());
		}

		return GetOrderDetailRes.builder()
			.items(items)
			.orderId(order.getId())
			.userId(order.getUser().getId())
			.totalAmount(order.getTotalAmount())
			.usePoint(order.getUsePoint())
			.orderStatus(order.getStatus())
			.createTime(order.getCreatedAt())
			.updateTime(order.getUpdatedAt())
			.build();
	}

	@Transactional(readOnly = true)
	public GetOrderListRes getAllOrder(Long userId, Pageable pageable) {

		User user = getUser(userId);

		validatePageable(pageable);

		Page<Order> orderPage = orderRepository.findByUser(user, pageable);

		List<OrderListItemRes> orders = orderPage.getContent().stream()
			.map(OrderListItemRes::from)
			.toList();

		return GetOrderListRes.builder()
			.orders(orders)
			.page(orderPage.getNumber())
			.size(orderPage.getSize())
			.totalElements(orderPage.getTotalElements())
			.totalPages(orderPage.getTotalPages())
			.hasNext(orderPage.hasNext())
			.build();
	}


	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.USER_NOT_FOUND));
	}

	private Course getCourse(Long courseId) {
		return courseRepository.findById(courseId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.COURSE_NOT_FOUND));
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
	}

	private void validateAccess(User user, Order order) {
		if (!user.getId().equals(order.getUser().getId())) {
			log.warn("주문 조회 권한이 없습니다. userId = {}", user.getId());
			throw new CustomException(OrderErrorCode.UNAUTHORIZED_ACCESS);
		}
	}

	private void validateSufficientPoint(Long userId, BigDecimal userPoint, BigDecimal usePoint) {
		if (userPoint.compareTo(usePoint) < 0) {
			log.warn("보유 포인트 부족 : user_id={}, 사용자 보유 포인트={}, 사용 포인트={}",
				userId, userPoint, usePoint);
			throw new CustomException(OrderErrorCode.INSUFFICIENT_POINT);
		}
	}

	private void validatePageable(Pageable pageable) {

		int page = pageable.getPageNumber();
		int size = pageable.getPageSize();

		if (page < 0) {
			throw new CustomException(OrderErrorCode.BAD_PAGING_CONDITION);
		}

		if (size < 1 || size > 50) {
			throw new CustomException(OrderErrorCode.BAD_PAGING_CONDITION);
		}
	}
}