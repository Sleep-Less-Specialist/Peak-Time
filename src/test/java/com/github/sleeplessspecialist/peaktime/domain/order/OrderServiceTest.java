package com.github.sleeplessspecialist.peaktime.domain.order;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderItemReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.exception.OrderErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * OrderService 단위 테스트
 * <p>
 * 주문(Order) 도메인의 내부로직 단위 테스트
 * </p>
 *
 * @author 우재
 * @since 2026. 1. 27.
 */
@ExtendWith(MockitoExtension.class)
class OrderServiceTest {

	@InjectMocks
	private OrderService orderService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private OrderRepository orderRepository;

	@Mock
	private OrderItemRepository orderItemRepository;

	@Test
	@DisplayName("createOrder: totalAmount 합산, order/items 저장, 응답 필드 확인")
	void createOrder_success() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("우재", "test@test.com", "password1234", "010-0000-0000");
		user.addPoint(5000L);

		Course course1 = Course.builder()
			.title("course-1")
			.description("desc-1")
			.category("cat-1")
			.price(new BigDecimal("10000"))
			.thumbnailUrl("thumb-1")
			.lecturer(user)
			.build();
		ReflectionTestUtils.setField(course1, "id", 11L);

		Course course2 = Course.builder()
			.title("course-2")
			.description("desc-2")
			.category("cat-2")
			.price(new BigDecimal("15000"))
			.thumbnailUrl("thumb-2")
			.lecturer(user)
			.build();
		ReflectionTestUtils.setField(course2, "id", 12L);

		CreateOrderReq request = new CreateOrderReq(
			List.of(new CreateOrderItemReq(11L), new CreateOrderItemReq(12L)),
			new BigDecimal("1000")
		);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
		when(courseRepository.findById(11L)).thenReturn(java.util.Optional.of(course1));
		when(courseRepository.findById(12L)).thenReturn(java.util.Optional.of(course2));

		when(orderRepository.save(any(Order.class))).thenAnswer(invocation -> {
			Order order = invocation.getArgument(0);
			ReflectionTestUtils.setField(order, "id", 100L);
			return order;
		});

		when(orderItemRepository.save(any(OrderItem.class))).thenAnswer(invocation -> {
			OrderItem item = invocation.getArgument(0);
			if (item.getId() == null) {
				Long id = item.getCourse().getId() + 1000L;
				ReflectionTestUtils.setField(item, "id", id);
			}
			return item;
		});

		// when
		CreateOrderRes response = orderService.createOrder(userId, request);

		// then
		ArgumentCaptor<Order> orderCaptor = ArgumentCaptor.forClass(Order.class);
		verify(orderRepository).save(orderCaptor.capture());
		Order savedOrder = orderCaptor.getValue();
		assertThat(savedOrder.getTotalAmount()).isEqualByComparingTo("25000");
		assertThat(savedOrder.getUsePoint()).isEqualByComparingTo("1000");

		verify(orderItemRepository, times(2)).save(any(OrderItem.class));

		assertThat(response.getOrderId()).isEqualTo(100L);
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getTotalAmount()).isEqualByComparingTo("25000");
		assertThat(response.getUsePoint()).isEqualByComparingTo("1000");
		assertThat(response.getItems()).hasSize(2);

		assertThat(response.getItems().get(0).getCourseId()).isEqualTo(11L);
		assertThat(response.getItems().get(0).getOrderItemId()).isEqualTo(1011L);
		assertThat(response.getItems().get(0).getPrice()).isEqualByComparingTo("10000");

		assertThat(response.getItems().get(1).getCourseId()).isEqualTo(12L);
		assertThat(response.getItems().get(1).getOrderItemId()).isEqualTo(1012L);
		assertThat(response.getItems().get(1).getPrice()).isEqualByComparingTo("15000");
	}

	@Test
	@DisplayName("createOrder: 보유 포인트 부족이면 예외")
	void createOrder_insufficientPoint() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");

		CreateOrderReq request = new CreateOrderReq(
			List.of(new CreateOrderItemReq(11L)),
			new BigDecimal("1000")
		);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

		// when / then
		assertThatThrownBy(() -> orderService.createOrder(userId, request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(OrderErrorCode.INSUFFICIENT_POINT));

		verify(orderRepository, never()).save(any(Order.class));
		verify(orderItemRepository, never()).save(any(OrderItem.class));
	}
	
}
