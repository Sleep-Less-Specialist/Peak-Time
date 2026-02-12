package com.github.sleeplessspecialist.peaktime.domain.notice.service;

import com.github.sleeplessspecialist.peaktime.domain.notice.entity.Notice;
import com.github.sleeplessspecialist.peaktime.domain.notice.exception.NoticeErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.notice.repository.NoticeRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.Order;
import com.github.sleeplessspecialist.peaktime.domain.order.entity.OrderItem;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import lombok.RequiredArgsConstructor;
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
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {

            String title = orderItem.getCourse().getTitle();
            var lecturer = orderItem.getCourse().getLecturer();

            String content = String.format(
                    "%s 님이 강의 %s 을(를) 구매했습니다.",
                    buyerName,
                    title
            );

            Notice notice = Notice.builder()
                    .user(lecturer)
                    .content(content)
                    .build();

            noticeRepository.save(notice);
        }
    }

    /**
     * 결제 취소 알림 생성
     */
    @Transactional
    public void createPaymentCanceledNotice(Long orderId) {

        Order order = getOrder(orderId);

        String buyerName = order.getUser().getName();
        List<OrderItem> orderItems = orderItemRepository.findAllByOrderId(orderId);

        for (OrderItem orderItem : orderItems) {

            String title = orderItem.getCourse().getTitle();
            var lecturer = orderItem.getCourse().getLecturer();

            String content = String.format(
                    "%s 님이 강의 %s 을(를) 결제 취소했습니다.",
                    buyerName,
                    title
            );

            Notice notice = Notice.builder()
                    .user(lecturer)
                    .content(content)
                    .build();

            noticeRepository.save(notice);
        }
    }

    private Order getOrder(Long orderId) {
        return orderRepository.findById(orderId)
                .orElseThrow(() -> new CustomException(NoticeErrorCode.ORDER_NOT_FOUND));
    }
}
