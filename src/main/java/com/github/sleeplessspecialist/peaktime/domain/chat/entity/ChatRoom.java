package com.github.sleeplessspecialist.peaktime.domain.chat.entity;

import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방(ChatRoom) 엔티티
 *
 * <p>
 * 커피쳇(1:1 또는 그룹 채팅)의 방 정보를 나타내는 도메인 엔티티이다.
 * 채팅방은 생성 → 모집(OPEN) → 매칭/진행(MATCHED or IN_PROGRESS) → 종료(CLOSED)
 * 와 같은 생명주기를 가진다.
 * </p>
 *
 * <p>
 * 이 엔티티는 "채팅 메시지"를 직접 소유하지 않으며,
 * 채팅 참여자(ChatParticipant), 채팅 메시지(ChatMessage)와
 * 1:N 관계의 루트 엔티티 역할을 한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatRoom extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String content;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private ChatRoomStatus chatRoomStatus = ChatRoomStatus.OPEN;

	@Column(nullable = false)
	private boolean isGroupChat;

	@Builder
	public ChatRoom(String content) {
		this.content = content;
		this.chatRoomStatus = ChatRoomStatus.OPEN;
		this.isGroupChat = false;
	}
}
