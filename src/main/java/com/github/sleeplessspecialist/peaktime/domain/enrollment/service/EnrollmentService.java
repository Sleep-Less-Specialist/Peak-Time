package com.github.sleeplessspecialist.peaktime.domain.enrollment.service;

import java.util.List;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.exception.EnrollmentErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 수강 등록 서비스.
 * <p>
 * 결제 완료된 주문을 기반으로 수강(Enrollment)을 생성한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class EnrollmentService {

	private final OrderRepository orderRepository;
	private final EnrollmentRepository enrollmentRepository;
	private final OrderItemRepository orderItemRepository;

	@Transactional
	public void createEnrollment(Long orderId) {

		Order order = getOrder(orderId);

		List<OrderItem> orderItems = orderItemRepository.findOrderItemWithCourseByOrderId(orderId);

		for (OrderItem item : orderItems) {

			Enrollment enrollment = Enrollment.builder()
				.course(item.getCourse())
				.user(order.getUser())
				.build();

			try {
				enrollmentRepository.save(enrollment);
			} catch (DataIntegrityViolationException e) {
				log.debug("이미 수강 등록이 존재합니다. orderId={}, courseId={}, userId={}",
					orderId, item.getCourse().getId(), order.getUser().getId());
			}
		}
	}

	@Transactional
	public void cancelEnrollment(Long orderId) {

        Order order = getOrder(orderId);
        Long userId = order.getUser().getId();

		List<OrderItem> orderItems = orderItemRepository.findOrderItemWithCourseByOrderId(orderId);

		List<Long> courseIds = orderItems.stream()
			.map(orderItem -> orderItem.getCourse().getId())
			.distinct()
			.toList();

		List<Enrollment> enrollments = enrollmentRepository
			.findAllByUserIdAndCourseIdIn(userId, courseIds);

		for(Enrollment enrollment : enrollments) {
			enrollment.cancelled();
		}
	}

	private Order getOrder(Long orderId) {
		return orderRepository.findById(orderId)
			.orElseThrow(() -> new CustomException(EnrollmentErrorCode.ORDER_NOT_FOUND));
	}
}
