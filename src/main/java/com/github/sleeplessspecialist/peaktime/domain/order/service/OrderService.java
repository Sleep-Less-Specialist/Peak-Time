package com.github.sleeplessspecialist.peaktime.domain.order.service;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
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
@Service
@RequiredArgsConstructor
@Slf4j
public class OrderService {

	private final UserRepository userRepository;
	private final CourseRepository courseRepository;
	private final OrderRepository orderRepository;
	private final OrderItemRepository orderItemRepository;
	private final EnrollmentRepository enrollmentRepository;

	/**
	 * 주문 생성
	 * 1. userId DB 존재 여부 확인
	 * 2. 사용 포인트 유효성 검증
	 * 3. 중복 구매 방지 (이미 구매한 강의 재구매 하기) enrollment 에 내역이 존재하는 강의 주문
	 * 4. 자신의 강의 구매 (course 의 userId 와 현재 주문하는 userId 가 일치하는 경우)
	 */
	@Transactional
	public CreateOrderRes createOrder(Long userId, CreateOrderReq request) {

		User user = getUser(userId);

		BigDecimal totalAmount = BigDecimal.ZERO;
		BigDecimal usePoint = request.getUsePoint();
		BigDecimal userPoint = BigDecimal.valueOf(user.getPoint());

		validateSufficientPoint(userId, userPoint, usePoint);

		List<Long> courseIds = getCourseIds(request);

		Map<Long, Course> courseMap = validateAndLoadCourses(userId, courseIds);  //4. 자신의 강의 구매

		validateAlreadyEnrolled(userId, courseIds);  //3. 중복 구매 방지

		Order order = createOrderEntity(request, user, totalAmount);

		List<OrderItemRes> orderItems = new ArrayList<>();

		for (CreateOrderItemReq item : request.getOrderItems()) {
			Course course = courseMap.get(item.getCourseId());

			BigDecimal coursePrice = course.getPrice();
			totalAmount = totalAmount.add(coursePrice);

			OrderItem saved = orderItemRepository.save(
				OrderItem.builder()
					.order(order)
					.course(course)
					.price(coursePrice)
					.build()
			);

			orderItems.add(OrderItemRes.builder()
				.orderItemId(saved.getId())
				.courseId(course.getId())
				.price(coursePrice)
				.build());
		}

		validateUsePoint(totalAmount, usePoint);
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

	/**
	 * 1. userId DB 존재 여부 확인
	 * 2. Pageable 객체 변환
	 * 3. 쿼리메서드 호출후 응답 dto 로 리턴
	 */
	@Transactional(readOnly = true)
	public GetOrderListRes getAllOrder(Long userId, int page, int size) {

		User user = getUser(userId);

		Pageable pageable = PageRequest.of(
			page - 1,
			size,
			Sort.by(Sort.Direction.DESC, "createdAt")
		);

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

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(OrderErrorCode.ORDER_NOT_FOUND));
	}

	private Order createOrderEntity(CreateOrderReq request, User user, BigDecimal totalAmount) {
		Order order = Order.builder()
			.user(user)
			.totalAmount(totalAmount)
			.usePoint(request.getUsePoint())
			.build();

		orderRepository.save(order);
		return order;
	}

	private List<Long> getCourseIds(CreateOrderReq request) {
		List<Long> courseIds = request.getOrderItems().stream()
			.map(CreateOrderItemReq::getCourseId)
			.distinct()
			.toList();
		return courseIds;
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

	private void validateUsePoint(BigDecimal totalAmount, BigDecimal usePoint) {
		if (totalAmount.compareTo(usePoint) < 0) {
			log.warn("사용 포인트가 주문 금액을 초과 : 사용 포인트 = {}, totalAmount = {} ",
				usePoint, totalAmount);
			throw new CustomException(OrderErrorCode.INVALID_USE_POINT);
		}
	}

	private Map<Long, Course> validateAndLoadCourses(Long userId, List<Long> courseIds) {

		List<Course> courses = courseRepository.findAllByIdInWithUser(courseIds);

		Map<Long, Course> courseMap = new HashMap<>();

		for (Course course : courses) {

			if (course.getLecturer().getId().equals(userId)) {
				log.warn(
					"자신의 강의 구매 시도 차단: userId={}, courseId={}",
					userId,
					course.getId()
				);
				throw new CustomException(OrderErrorCode.CANNOT_BUY_OWN_COURSE);
			}
			courseMap.put(course.getId(), course);
		}

		return courseMap;
	}

	private void validateAlreadyEnrolled(Long userId, List<Long> courseIds) {
		if (enrollmentRepository.existsByUserIdAndCourseIdIn(userId, courseIds)) {
			log.warn(
				"이미 수강 중인 강의 재구매 시도 차단: userId={}, courseIds={}",
				userId,
				courseIds
			);
			throw new CustomException(OrderErrorCode.ALREADY_ENROLLED_COURSE);
		}
	}

}