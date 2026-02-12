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
@Table(name = "notices")
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

    @Builder
    public Notice(User user, String content) {
        this.user = user;
        this.content = content;
    }
}
