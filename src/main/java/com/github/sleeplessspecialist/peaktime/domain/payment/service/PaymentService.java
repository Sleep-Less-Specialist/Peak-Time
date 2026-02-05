package com.github.sleeplessspecialist.peaktime.domain.payment.service;

import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.service.OrderPaymentCommandService;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.*;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.Payment;
import com.github.sleeplessspecialist.peaktime.domain.payment.entity.PaymentStatus;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentCancelledEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.event.PaymentConfirmedEvent;
import com.github.sleeplessspecialist.peaktime.domain.payment.exception.PaymentErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.payment.repository.PaymentRepository;
import com.github.sleeplessspecialist.peaktime.domain.payment.utill.OrderIdParser;
import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.refund.service.RefundService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.payment.TossPaymentClient;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

/**
 * 결제 도메인의 비즈니스 로직을 담당하는 서비스 클래스입니다.
 * <p>
 * 결제 승인 요청 시 트랜잭션을 관리하며, 외부 결제 클라이언트(TossPaymentClient)를 호출하여
 * 실제 결제 승인을 수행하고 결과를 반환합니다. 추후 주문 상태 업데이트 로직이 포함됩니다.
 * </p>
 *
 * @author 기섭, 주우재
 * @version 1.1
 * @since 2026. 1. 31.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PaymentService {

    private final OrderRepository orderRepository;
    private final TossPaymentClient tossPaymentClient;
    private final PointService pointService;
    private final ApplicationEventPublisher eventPublisher;
    private final PaymentRepository paymentRepository;
    private final OrderPaymentCommandService orderPaymentCommandService;
    private final RefundService refundService;

    @Transactional
    public void confirmPayment(TossPaymentConfirmReq req) {

        TossPaymentConfirmRes result = tossPaymentClient.confirm(req); // 1) toss 결제 승인 (동기)

        Long orderId = Long.valueOf(OrderIdParser.extractOrderId(result.getOrderId()));
        Order order = getOrder(orderId);
        Long usePoint = order.getUsePoint().longValueExact();
        BigDecimal finalAmount = result.getTotalAmount();
        String impUid = result.getPaymentKey();
        String paymentMethod = result.getMethod();

        try {
            pointService.spendForOrder(order.getUser(), orderId, usePoint); // 2) 포인트 차감 (동기)

            // 3) payment 생성후 DB 저장 (동기)
            Payment payment = Payment.builder()
                    .order(order)
                    .amount(finalAmount)
                    .paymentMethod(paymentMethod)
                    .status(PaymentStatus.PAID)
                    .impUid(impUid)
                    .build();

            savePayment(payment);

            // 4) order 상태 전이 (동기)
            orderPaymentCommandService.markCompleted(orderId);
            // 보상 트랜잭션
        } catch (Exception afterConfirmFailed) {
            try {
                TossPaymentCancelReq cancelReq = TossPaymentCancelReq.builder()
                        .cancelReason("결제 서버 오류로 인한 취소")
                        .build();

                tossPaymentClient.cancel(impUid, cancelReq);

            } catch (Exception cancelFailed) {
                log.error("보상 취소 실패. orderId={}, paymentKey={}", orderId, impUid, cancelFailed);
            }
        }

        // 5) 비동기 작업 트리거 (커밋 이후 실행되도록 리스너에서 AFTER_COMMIT 사용)
        eventPublisher.publishEvent(PaymentConfirmedEvent.builder()
                .orderId(order.getId())
                .userId(order.getUser().getId())
                .build());
    }

    @Transactional
    public void cancelPayment(PaymentCancelReq req) {

        TossPaymentCancelReq tossPaymentCancelReq = TossPaymentCancelReq.builder()
                .cancelReason(req.getCancelReason())
                .build();

        // 1) toss 결제 취소(동기)
        TossPaymentCancelRes result = tossPaymentClient.cancel(req.getPaymentKey(), tossPaymentCancelReq);

        Long orderId = Long.valueOf(OrderIdParser.extractOrderId(result.getOrderId()));
        Order order = getOrder(orderId);
        Payment payment = getPayment(orderId);
        String cancelReason = result.getCancelReason();
        BigDecimal cancelAmount = result.getCancelAmount();
        Long refundPoint = order.getUsePoint().longValueExact();

        try {
            pointService.refundForOrder(order.getUser(), orderId, refundPoint); // 2) 포인트 복구 (동기)

            // 4) Payment / Order 상태 전이 (동기)
            payment.refund();
            orderPaymentCommandService.markCanceled(orderId);

            // 5) Refund 저장 (동기)
            refundService.createRefund(payment, cancelAmount, cancelReason);
        } catch (Exception localFailed) {
            log.error(
                    "결제 취소는 완료되었으나, 후처리 중 오류가 발생했습니다. orderId={}, paymentKey={}, refundPoint={} ",
                    orderId, req.getPaymentKey(), localFailed
            );
            throw localFailed;
        }
        // 6) 비동기 작업 트리거 (커밋 이후 실행되도록 리스너에서 AFTER_COMMIT 사용)
        eventPublisher.publishEvent(PaymentCancelledEvent.builder()
                .orderId(orderId)
                .userId(order.getUser().getId())
                .build());

    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.ORDER_NOT_FOUND));
    }

    private Payment getPayment(Long orderId) {
        return paymentRepository.findByOrderId(orderId)
                .orElseThrow(() -> new CustomException(PaymentErrorCode.PAYMENT_NOT_FOUND));
    }

    private void savePayment(Payment payment) {
        try {
            paymentRepository.save(payment);
        } catch (DataIntegrityViolationException e) {
            log.debug(
                    "이미 결제 완료된 주문 입니다. orderId={}, paymentKey={}",
                    payment.getOrder().getId(),
                    payment.getImpUid()
            );
            throw new CustomException(PaymentErrorCode.ALREADY_ENROLLED_PAYMENT);
        }
    }
}