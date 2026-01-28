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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderItemReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderReq;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.CreateOrderRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderDetailRes;
import com.github.sleeplessspecialist.peaktime.domain.order.dto.GetOrderListRes;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderStatus;
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

	@Test
	@DisplayName("getOrder: 주문 상세 조회 성공")
	void getOrder_success() {
		// given
		Long userId = 1L;
		Long orderId = 100L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

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

		Order order = Order.builder()
			.user(user)
			.totalAmount(new BigDecimal("25000"))
			.usePoint(new BigDecimal("1000"))
			.build();
		ReflectionTestUtils.setField(order, "id", orderId);
		ReflectionTestUtils.setField(order, "status", OrderStatus.PENDING_PAYMENT);
		ReflectionTestUtils.setField(order, "createdAt", java.time.LocalDateTime.of(2026, 1, 27, 10, 0));
		ReflectionTestUtils.setField(order, "updatedAt", java.time.LocalDateTime.of(2026, 1, 27, 10, 5));

		OrderItem item1 = OrderItem.builder()
			.order(order)
			.course(course1)
			.price(course1.getPrice())
			.build();
		ReflectionTestUtils.setField(item1, "id", 1011L);

		OrderItem item2 = OrderItem.builder()
			.order(order)
			.course(course2)
			.price(course2.getPrice())
			.build();
		ReflectionTestUtils.setField(item2, "id", 1012L);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
		when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));
		when(orderItemRepository.findAllByOrderId(orderId)).thenReturn(List.of(item1, item2));

		// when
		GetOrderDetailRes response = orderService.getOrderDetail(userId, orderId);

		// then
		assertThat(response.getOrderId()).isEqualTo(orderId);
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getTotalAmount()).isEqualByComparingTo("25000");
		assertThat(response.getUsePoint()).isEqualByComparingTo("1000");
		assertThat(response.getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
		assertThat(response.getCreateTime()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 27, 10, 0));
		assertThat(response.getUpdateTime()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 27, 10, 5));
		assertThat(response.getItems()).hasSize(2);
		assertThat(response.getItems().get(0).getOrderItemId()).isEqualTo(1011L);
		assertThat(response.getItems().get(0).getCourseId()).isEqualTo(11L);
		assertThat(response.getItems().get(0).getPrice()).isEqualByComparingTo("10000");
		assertThat(response.getItems().get(1).getOrderItemId()).isEqualTo(1012L);
		assertThat(response.getItems().get(1).getCourseId()).isEqualTo(12L);
		assertThat(response.getItems().get(1).getPrice()).isEqualByComparingTo("15000");
	}

	@Test
	@DisplayName("getOrder: 다른 사용자 주문 조회 시 예외")
	void getOrder_unauthorizedAccess() {
		// given
		Long userId = 1L;
		Long orderId = 100L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		User otherUser = User.createForSignup("other", "other@test.com", "encoded", "010-0000-0001");
		ReflectionTestUtils.setField(otherUser, "id", 2L);

		Order order = Order.builder()
			.user(otherUser)
			.totalAmount(new BigDecimal("25000"))
			.usePoint(new BigDecimal("1000"))
			.build();
		ReflectionTestUtils.setField(order, "id", orderId);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
		when(orderRepository.findById(orderId)).thenReturn(java.util.Optional.of(order));

		// when / then
		assertThatThrownBy(() -> orderService.getOrderDetail(userId, orderId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(OrderErrorCode.UNAUTHORIZED_ACCESS));
	}

	@Test
	@DisplayName("getAllOrder: 주문 전체 조회 성공")
	void getAllOrder_success() {
		// given
		Long userId = 1L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		Order order1 = Order.builder()
			.user(user)
			.totalAmount(new BigDecimal("20000"))
			.usePoint(new BigDecimal("1000"))
			.build();
		ReflectionTestUtils.setField(order1, "id", 100L);
		ReflectionTestUtils.setField(order1, "status", OrderStatus.PENDING_PAYMENT);
		ReflectionTestUtils.setField(order1, "createdAt", java.time.LocalDateTime.of(2026, 1, 27, 9, 0));

		Order order2 = Order.builder()
			.user(user)
			.totalAmount(new BigDecimal("15000"))
			.usePoint(new BigDecimal("500"))
			.build();
		ReflectionTestUtils.setField(order2, "id", 101L);
		ReflectionTestUtils.setField(order2, "status", OrderStatus.PENDING_PAYMENT);
		ReflectionTestUtils.setField(order2, "createdAt", java.time.LocalDateTime.of(2026, 1, 27, 10, 0));

		PageRequest pageable = PageRequest.of(0, 2);
		Page<Order> orderPage = new PageImpl<>(List.of(order1, order2), pageable, 4);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));
		when(orderRepository.findByUser(user, pageable)).thenReturn(orderPage);

		// when
		GetOrderListRes response = orderService.getAllOrder(userId, pageable);

		// then
		assertThat(response.getOrders()).hasSize(2);

		assertThat(response.getOrders().get(0).getOrderId()).isEqualTo(100L);
		assertThat(response.getOrders().get(0).getUserId()).isEqualTo(userId);
		assertThat(response.getOrders().get(0).getTotalAmount()).isEqualByComparingTo("20000");
		assertThat(response.getOrders().get(0).getUserPoint()).isEqualByComparingTo("1000");
		assertThat(response.getOrders().get(0).getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
		assertThat(response.getOrders().get(0).getCreateAt()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 27, 9, 0));

		assertThat(response.getOrders().get(1).getOrderId()).isEqualTo(101L);
		assertThat(response.getOrders().get(1).getUserId()).isEqualTo(userId);
		assertThat(response.getOrders().get(1).getTotalAmount()).isEqualByComparingTo("15000");
		assertThat(response.getOrders().get(1).getUserPoint()).isEqualByComparingTo("500");
		assertThat(response.getOrders().get(1).getOrderStatus()).isEqualTo(OrderStatus.PENDING_PAYMENT);
		assertThat(response.getOrders().get(1).getCreateAt()).isEqualTo(java.time.LocalDateTime.of(2026, 1, 27, 10, 0));

		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getSize()).isEqualTo(2);
		assertThat(response.getTotalElements()).isEqualTo(4);
		assertThat(response.getTotalPages()).isEqualTo(2);
		assertThat(response.isHasNext()).isTrue();
	}

	@Test
	@DisplayName("getAllOrder: 페이지 조건 불일치 예외")
	void getAllOrder_invalidPageSize() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		PageRequest pageable = PageRequest.of(0, 51);

		when(userRepository.findById(userId)).thenReturn(java.util.Optional.of(user));

		// when / then
		assertThatThrownBy(() -> orderService.getAllOrder(userId, pageable))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(OrderErrorCode.BAD_PAGING_CONDITION));
	}
}
