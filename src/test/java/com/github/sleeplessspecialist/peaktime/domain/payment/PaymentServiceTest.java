package com.github.sleeplessspecialist.peaktime.domain.payment;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import com.github.sleeplessspecialist.peaktime.domain.payment.service.PaymentService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderPaymentCommandService;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.PaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossCancelInfo;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.PaymentStatus;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentCancelledEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.exception.PaymentErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.payment.repository.PaymentRepository;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.refund.service.RefundService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.payment.TossPaymentClient;

/**
 * {@link PaymentService}의 결제 승인/취소 핵심 플로우를 검증하는 테스트 클래스입니다.
 * <p>
 * 외부 결제사(Toss) 호출 결과에 따라 포인트 차감/복구, 결제/주문 상태 전이,
 * 환불 저장 및 도메인 이벤트 발행이 기대한 순서와 조건으로 수행되는지 확인합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 2. 5.
 */
@ExtendWith(MockitoExtension.class)
class PaymentServiceTest {

    @InjectMocks
    private PaymentService paymentService;

    @Mock
    private OrderRepository orderRepository;

    @Mock
    private TossPaymentClient tossPaymentClient;

    @Mock
    private PointService pointService;

    @Mock
    private ApplicationEventPublisher eventPublisher;

    @Mock
    private PaymentRepository paymentRepository;

    @Mock
    private OrderPaymentCommandService orderPaymentCommandService;

    @Mock
    private RefundService refundService;

    /**
     * 결제 승인 성공 케이스를 검증합니다.
     * <p>
     * Toss 결제 승인 성공 시 포인트 차감 → 결제 저장 → 주문 상태 전이 →
     * 결제 확정 이벤트 발행이 정상적으로 수행되는지 확인합니다.
     * </p>
     */
    @Test
    @DisplayName("결제 승인 성공: 포인트 차감/결제 저장/주문 완료 전이/이벤트 발행")
    void confirmPayment_success() {
        // given
        Long orderId = 100L;
        Long userId = 10L;
        BigDecimal usePoint = new BigDecimal("1000");

        User user = User.createForSignup("tester", "test@test.com", "pw", "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("20000"))
                .usePoint(usePoint)
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        String tossOrderId = "ORDER_20260130101010_100_ABCD";
        String paymentKey = "pay_123";
        String method = "CARD";
        BigDecimal totalAmount = new BigDecimal("20000");

        TossPaymentConfirmReq req = TossPaymentConfirmReq.builder()
                .paymentKey(paymentKey)
                .orderId(tossOrderId)
                .amount(20000L)
                .build();

        TossPaymentConfirmRes res = new TossPaymentConfirmRes(
                paymentKey,
                tossOrderId,
                "orderName",
                "DONE",
                method,
                totalAmount,
                "2026-01-30T10:10:10",
                "2026-01-30T10:10:20"
        );

        when(tossPaymentClient.confirm(req)).thenReturn(res);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenAnswer(invocation -> invocation.getArgument(0));

        // when
        paymentService.confirmPayment(req);

        // then
        verify(pointService).spendForOrder(user, orderId, 1000L);
        verify(orderPaymentCommandService).markCompleted(orderId);
        verify(tossPaymentClient, never()).cancel(anyString(), any(TossPaymentCancelReq.class));

        ArgumentCaptor<Payment> paymentCaptor = ArgumentCaptor.forClass(Payment.class);
        verify(paymentRepository).save(paymentCaptor.capture());
        Payment saved = paymentCaptor.getValue();
        assertThat(saved.getOrder()).isEqualTo(order);
        assertThat(saved.getAmount()).isEqualByComparingTo("20000");
        assertThat(saved.getPaymentMethod()).isEqualTo(method);
        assertThat(saved.getStatus()).isEqualTo(PaymentStatus.PAID);
        assertThat(saved.getImpUid()).isEqualTo(paymentKey);

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(PaymentConfirmedEvent.class);
        PaymentConfirmedEvent event = (PaymentConfirmedEvent) eventCaptor.getValue();
        assertThat(event.getOrderId()).isEqualTo(orderId);
        assertThat(event.getUserId()).isEqualTo(userId);
    }

    /**
     * 결제 승인 실패(주문 없음) 케이스를 검증합니다.
     * <p>
     * Toss 결제 승인 응답을 받았더라도, 내부 주문(Order)을 찾지 못하면
     * {@link CustomException}이 발생하며 이후 로직(포인트 차감/결제 저장/이벤트 발행)이 수행되지 않아야 합니다.
     * </p>
     */
    @Test
    @DisplayName("결제 승인 실패: 주문을 찾을 수 없음")
    void confirmPayment_orderNotFound() {
        // given
        String tossOrderId = "ORDER_20260130101010_999_ABCD";
        TossPaymentConfirmReq req = TossPaymentConfirmReq.builder()
                .paymentKey("pay_999")
                .orderId(tossOrderId)
                .amount(20000L)
                .build();

        TossPaymentConfirmRes res = new TossPaymentConfirmRes(
                "pay_999",
                tossOrderId,
                "orderName",
                "DONE",
                "CARD",
                new BigDecimal("20000"),
                "2026-01-30T10:10:10",
                "2026-01-30T10:10:20"
        );

        when(tossPaymentClient.confirm(req)).thenReturn(res);
        when(orderRepository.findById(999L)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> paymentService.confirmPayment(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(PaymentErrorCode.ORDER_NOT_FOUND));

        verify(pointService, never()).spendForOrder(any(), anyLong(), anyLong());
        verify(paymentRepository, never()).save(any(Payment.class));
        verify(eventPublisher, never()).publishEvent(any());
    }

    /**
     * 결제 승인 중복(유니크 제약) 케이스를 검증합니다.
     * <p>
     * 결제 저장 시 {@link DataIntegrityViolationException}이 발생하면
     * 중복 결제로 판단하고 예외를 발생시키는지 확인합니다.
     * </p>
     */
    @Test
    @DisplayName("결제 승인 실패: 이미 결제된 주문(중복 결제)")
    void confirmPayment_duplicatePayment_thenCancel() {
        // given
        Long orderId = 100L;
        Long userId = 10L;
        BigDecimal usePoint = new BigDecimal("1000");

        User user = User.createForSignup("tester", "test@test.com", "pw", "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("20000"))
                .usePoint(usePoint)
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        String tossOrderId = "ORDER_20260130101010_100_ABCD";
        String paymentKey = "pay_123";

        TossPaymentConfirmReq req = TossPaymentConfirmReq.builder()
                .paymentKey(paymentKey)
                .orderId(tossOrderId)
                .amount(20000L)
                .build();

        TossPaymentConfirmRes res = new TossPaymentConfirmRes(
                paymentKey,
                tossOrderId,
                "orderName",
                "DONE",
                "CARD",
                new BigDecimal("20000"),
                "2026-01-30T10:10:10",
                "2026-01-30T10:10:20"
        );

        when(tossPaymentClient.confirm(req)).thenReturn(res);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.save(any(Payment.class))).thenThrow(new DataIntegrityViolationException("dup"));
        when(tossPaymentClient.cancel(eq(paymentKey), any(TossPaymentCancelReq.class)))
                .thenReturn(TossPaymentCancelRes.builder().orderId(tossOrderId).cancels(List.of()).build());

        // when
        paymentService.confirmPayment(req);

        // then
        verify(pointService).spendForOrder(user, orderId, 1000L);
        verify(orderPaymentCommandService, never()).markCompleted(anyLong());
        verify(tossPaymentClient).cancel(eq(paymentKey), any(TossPaymentCancelReq.class));

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(PaymentConfirmedEvent.class);
    }

    /**
     * 결제 취소 성공 케이스를 검증합니다.
     * <p>
     * Toss 결제 취소 성공 시 포인트 복구 → 결제/주문 상태 전이 → 환불 저장 →
     * 결제 취소 이벤트 발행이 정상적으로 수행되는지 확인합니다.
     * </p>
     */
    @Test
    @DisplayName("결제 취소 성공: 포인트 복구/상태 전이/환불 저장/이벤트 발행")
    void cancelPayment_success() {
        // given
        Long orderId = 100L;
        Long userId = 10L;

        User user = User.createForSignup("tester", "test@test.com", "pw", "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("20000"))
                .usePoint(new BigDecimal("1000"))
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        Payment payment = Payment.builder()
                .order(order)
                .amount(new BigDecimal("20000"))
                .impUid("pay_123")
                .status(PaymentStatus.PAID)
                .paymentMethod("CARD")
                .build();

        String tossOrderId = "ORDER_20260130101010_100_ABCD";
        TossPaymentCancelRes res = TossPaymentCancelRes.builder()
                .orderId(tossOrderId)
                .cancels(List.of(new TossCancelInfo("user", new BigDecimal("20000"))))
                .build();

        PaymentCancelReq req = PaymentCancelReq.builder()
                .paymentKey("pay_123")
                .cancelReason("user")
                .build();

        when(tossPaymentClient.cancel(eq("pay_123"), any(TossPaymentCancelReq.class))).thenReturn(res);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.of(payment));

        // when
        paymentService.cancelPayment(req);

        // then
        verify(pointService).refundForOrder(user, orderId, 1000L);
        verify(orderPaymentCommandService).markCanceled(orderId);
        verify(refundService).createRefund(payment, new BigDecimal("20000"), "user");

        ArgumentCaptor<Object> eventCaptor = ArgumentCaptor.forClass(Object.class);
        verify(eventPublisher).publishEvent(eventCaptor.capture());
        assertThat(eventCaptor.getValue()).isInstanceOf(PaymentCancelledEvent.class);

        assertThat(payment.getStatus()).isEqualTo(PaymentStatus.REFUNDED);
    }

    /**
     * 결제 취소 실패(결제 없음) 케이스를 검증합니다.
     * <p>
     * Toss 결제 취소 응답을 받았더라도, 내부 결제(Payment)를 찾지 못하면
     * {@link CustomException}이 발생하며 이후 로직(포인트 복구/주문 전이/환불 저장/이벤트 발행)이 수행되지 않아야 합니다.
     * </p>
     */
    @Test
    @DisplayName("결제 취소 실패: 결제를 찾을 수 없음")
    void cancelPayment_paymentNotFound() {
        // given
        Long orderId = 100L;
        Long userId = 10L;

        User user = User.createForSignup("tester", "test@test.com", "pw", "010-0000-0000");
        ReflectionTestUtils.setField(user, "id", userId);

        Order order = Order.builder()
                .user(user)
                .totalAmount(new BigDecimal("20000"))
                .usePoint(new BigDecimal("1000"))
                .build();
        ReflectionTestUtils.setField(order, "id", orderId);

        String tossOrderId = "ORDER_20260130101010_100_ABCD";
        TossPaymentCancelRes res = TossPaymentCancelRes.builder()
                .orderId(tossOrderId)
                .cancels(List.of(new TossCancelInfo("user", new BigDecimal("20000"))))
                .build();

        PaymentCancelReq req = PaymentCancelReq.builder()
                .paymentKey("pay_123")
                .cancelReason("user")
                .build();

        when(tossPaymentClient.cancel(eq("pay_123"), any(TossPaymentCancelReq.class))).thenReturn(res);
        when(orderRepository.findById(orderId)).thenReturn(Optional.of(order));
        when(paymentRepository.findByOrderId(orderId)).thenReturn(Optional.empty());

        // when / then
        assertThatThrownBy(() -> paymentService.cancelPayment(req))
                .isInstanceOf(CustomException.class)
                .satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
                        .isEqualTo(PaymentErrorCode.PAYMENT_NOT_FOUND));

        verify(pointService, never()).refundForOrder(any(), anyLong(), anyLong());
        verify(orderPaymentCommandService, never()).markCanceled(anyLong());
        verify(refundService, never()).createRefund(any(), any(), anyString());
        verify(eventPublisher, never()).publishEvent(any());
    }
}
