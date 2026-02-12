package com.github.sleeplessspecialist.peaktime.domain.payment.service;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderPaymentCommandService;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.*;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.PaymentStatus;
import com.github.sleeplessspecialist.peaktime.domain.payment.exception.PaymentErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.payment.repository.PaymentRepository;
import com.github.sleeplessspecialist.peaktime.domain.payment.utill.OrderIdParser;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.refund.service.RefundService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEvent;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.entity.OutboxEventType;
import com.github.sleeplessspecialist.peaktime.global.infra.outbox.repository.OutboxRepository;
import com.github.sleeplessspecialist.peaktime.global.infra.payment.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.util.List;

/**
 * 결제 도메인의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 *
 * <p>
 * 외부 결제사(Toss) 호출은 네트워크 I/O로 지연/실패 가능성이 높으므로 DB 트랜잭션과 분리하여 수행합니다.
 * 로컬 반영(포인트/결제/주문 상태 전이/환불 생성)은 짧은 트랜잭션으로 처리하며,
 * 로컬 반영이 성공한 경우에만 이벤트를 발행합니다.
 * </p>
 *
 * @author 기섭, 주우재
 * @version 1.2
 * @since 2026.02.08
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final UserRepository userRepository;
    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PointService pointService;
    private final PaymentRepository paymentRepository;
    private final OrderPaymentCommandService orderPaymentCommandService;
    private final RefundService refundService;
    private final OutboxRepository outboxRepository;

    /**
     * 결제 확정.
     */
    public void confirmPayment(TossPaymentConfirmReq req) {

        TossPaymentConfirmRes res = tossPaymentClient.confirm(req);
        try {
            confirmLocalAndWriteOutbox(res);
        } catch (Exception localFailed) {
            try {
                TossPaymentCancelReq cancelReq = TossPaymentCancelReq.builder()
                        .cancelReason("로컬 후처리 실패로 인한 자동 취소")
                        .build();

                tossPaymentClient.cancel(res.getPaymentKey(), cancelReq);
            } catch (Exception cancelFailed) {
                log.error(
                        "[Compensation Required] 로컬 결제 후처리 실패. paymentKey={}, orderId={}",
                        res.getPaymentKey(),
                        res.getOrderId(),
                        localFailed
                );
            }
            throw localFailed;
        }
    }

    /**
     * 결제 취소.
     */
    public void cancelPayment(PaymentCancelReq req) {

        TossPaymentCancelRes res = tossPaymentClient.cancel(
                req.getPaymentKey(),
                TossPaymentCancelReq.builder().cancelReason(req.getCancelReason()).build()
        );

        try {
            cancelLocalAndWriteOutbox(res);
        } catch (Exception localFailed) {
            log.error(
                    "[Critical Compensation Failure] 외부 결제 취소까지 실패, 원인={}",
                    localFailed.getMessage(),
                    localFailed
            );
            throw localFailed;
        }
    }

    /**
     * 결제 확정 로컬 후처리 (트랜잭션).
     * - OutBox DB 에 저장
     */
    @Transactional
    protected void confirmLocalAndWriteOutbox(TossPaymentConfirmRes res) {

        Long orderId = parseOrderId(res.getOrderId());
        Order order = getOrder(orderId);
        Long usePoint = order.getUsePoint().longValueExact();
        BigDecimal finalAmount = res.getTotalAmount();

        pointService.spendForOrder(order.getUser(), orderId, usePoint);
        Payment payment = Payment.builder()
                .order(order)
                .amount(finalAmount)
                .paymentMethod(res.getMethod())
                .status(PaymentStatus.PAID)
                .impUid(res.getPaymentKey())
                .build();

        savePayment(payment);
        orderPaymentCommandService.markCompleted(orderId);

        outboxRepository.save(
                OutboxEvent.pending(
                        OutboxEventType.PAYMENT_CONFIRMED,
                        orderId
                )
        );
    }

    /**
     * 결제 취소 로컬 후처리 (트랜잭션).
     * - OutBox DB 에 저장
     */
    @Transactional
    protected void cancelLocalAndWriteOutbox(TossPaymentCancelRes res) {

        Long orderId = parseOrderId(res.getOrderId());
        Order order = getOrder(orderId);
        Payment payment = getPayment(orderId);
        Long refundPoint = order.getUsePoint().longValueExact();
        BigDecimal cancelAmount = res.getCancelAmount();
        String cancelReason = res.getCancelReason();

        pointService.refundForOrder(order.getUser(), orderId, refundPoint);
        payment.refund();
        orderPaymentCommandService.markCanceled(orderId);
        refundService.createRefund(payment, cancelAmount, cancelReason);

        outboxRepository.save(
                OutboxEvent.pending(
                        OutboxEventType.PAYMENT_CANCELED,
                        orderId
                )
        );
    }

    /**
     * 내 결제 전체 조회
     */
    @Transactional(readOnly = true)
    public GetMyPaymentListRes getMyPayments(Long userId, int page, int size) {

        User user = getUser(userId);

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Payment> paymentPage = paymentRepository.findPaymentsByUser(user, pageable);

        List<PaymentListItemRes> payments = paymentPage.getContent().stream()
                .map(PaymentListItemRes::from)
                .toList();

        return GetMyPaymentListRes.builder()
                .payments(payments)
                .page(paymentPage.getNumber())
                .size(paymentPage.getSize())
                .totalElements(paymentPage.getTotalElements())
                .totalPages(paymentPage.getTotalPages())
                .hasNext(paymentPage.hasNext())
                .build();
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.USER_NOT_FOUND));
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.ORDER_NOT_FOUND));
    }

    private Payment getPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private Long parseOrderId(String tossOrderId) {
        return Long.valueOf(OrderIdParser.extractOrderId(tossOrderId));
    }

    private void savePayment(Payment payment) {
        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            log.debug(
                    "이미 결제 완료된 주문입니다. orderId={}, paymentKey={}",
                    payment.getOrder().getId(),
                    payment.getImpUid()
            );
            throw new CustomException(PaymentErrorCode.ALREADY_ENROLLED_PAYMENT);
        }
    }
}
