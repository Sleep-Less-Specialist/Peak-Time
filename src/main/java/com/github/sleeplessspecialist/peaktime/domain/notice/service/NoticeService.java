package com.github.sleeplessspecialist.peaktime.domain.notice.service;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.notice.entity.Notice;
import com.github.sleeplessspecialist.peaktime.domain.notice.exception.NoticeErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.notice.repository.NoticeRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 결제 이벤트 기반 Notice 생성 커맨드 서비스입니다.
 *
 * <p>
 * orderId를 기준으로 주문 정보를 조회한 뒤,
 * 주문에 포함된 강의(Course)의 강사(lecturer)에게
 * 결제 완료 알림을 생성하여 저장합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.12
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class NoticeService {

    private final OrderRepository orderRepository;
    private final NoticeRepository noticeRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 결제 완료 알림 생성
     */
    @Transactional
    public void createPaymentConfirmedNotice(Long orderId) {

        Order order = getOrder(orderId);

        String buyerName = order.getUser().getName();
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsWithCourseAndLecturerByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {

            Course course = orderItem.getCourse();
            User lecturer = orderItem.getCourse().getLecturer();

            Notice notice = Notice.paymentConfirmed(
                    lecturer,
                    buyerName,
                    course.getTitle(),
                    orderId,
                    course.getId()
            );

            try {
                noticeRepository.save(notice);
            } catch (DataIntegrityViolationException e) {
                log.debug(
                        "Notice 중복 생성 감지 (멱등 처리). orderId={}, courseId={}, type={}",
                        orderId,
                        course.getId(),
                        "PAYMENT_CONFIRMED"
                );
            }
        }
    }

    /**
     * 결제 취소 알림 생성
     */
    @Transactional
    public void createPaymentCanceledNotice(Long orderId) {

        Order order = getOrder(orderId);

        String buyerName = order.getUser().getName();
        List<OrderItem> orderItems = orderItemRepository.findOrderItemsWithCourseAndLecturerByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {

            Course course = orderItem.getCourse();
            User lecturer = orderItem.getCourse().getLecturer();

            Notice notice = Notice.paymentCanceled(
                    lecturer,
                    buyerName,
                    course.getTitle(),
                    orderId,
                    course.getId()
            );

            try {
                noticeRepository.save(notice);
            } catch (DataIntegrityViolationException e) {
                log.debug(
                        "Notice 중복 생성 감지 (멱등 처리). orderId={}, courseId={}, type={}",
                        orderId,
                        course.getId(),
                        "PAYMENT_CANCELED"
                );
            }
        }
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.ORDER_NOT_FOUND));
    }
}
