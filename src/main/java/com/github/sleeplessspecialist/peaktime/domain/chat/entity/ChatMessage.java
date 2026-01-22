package com.github.sleeplessspecialist.peaktime.domain.chat.entity;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 메시지를 저장하는 엔티티
 * <p>
 * 메시지는 기본 생성 될때 기준으로 미 읽음 표시
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Entity
@Getter
@Table(name = "chat_messages")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatMessage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name ="chat_room_id",  nullable = false)
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column(nullable = false, length = 500)
	private String content;

	@Column(nullable = false)
	private boolean isRead = false;

	@Builder
	public ChatMessage(ChatRoom chatRoom, User user, String content) {
		this.chatRoom = chatRoom;
		this.user = user;
		this.content = content;
		this.isRead = false;
	}
}
