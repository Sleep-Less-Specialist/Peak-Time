package com.github.sleeplessspecialist.peaktime.domain.notice.entity;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 내 메시지 정보를 저장하는 엔티티입니다.
 *
 * <p>
 * 발신자, 메시지 내용, 읽음 여부를 관리하며
 * 생성 시 기본 unread 상태로 저장됩니다.
 * </p>
 *  @author 주우재
 *  @version 1.0
 *  @since 2026.02.12
 */
@Entity
@Getter
@Table(
        name = "notices",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_notice_dedup_key", columnNames = "dedup_key")
        }
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Notice extends BaseTimeEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false, length = 1000)
    private String content;

    @Column(name = "dedup_key", nullable = false, length = 100)
    private String dedupKey;

    public Notice(User user, String content, String dedupKey) {
        this.user = user;
        this.content = content;
        this.dedupKey = dedupKey;
    }
    /**
     * 결제 완료 알림 생성
     */
    public static Notice paymentConfirmed(
            User lecturer,
            String buyerName,
            String courseTitle,
            Long orderId,
            Long courseId
    ) {
        String dedupKey = "PAYMENT_CONFIRMED:" + orderId + ":" + courseId;

        String content = String.format(
                "%s님이 \"%s\" 강의를 구매했습니다.",
                buyerName,
                courseTitle
        );

        return new Notice(lecturer, content, dedupKey);
    }

    /**
     * 결제 취소 알림 생성
     */
    public static Notice paymentCanceled(
            User lecturer,
            String buyerName,
            String courseTitle,
            Long orderId,
            Long courseId
    ) {
        String dedupKey = "PAYMENT_CANCELED:" + orderId + ":" + courseId;

        String content = String.format(
                "%s님이 \"%s\" 강의를 결제 취소했습니다.",
                buyerName,
                courseTitle
        );

        return new Notice(lecturer, content, dedupKey);
    }
}
