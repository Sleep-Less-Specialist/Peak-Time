package com.github.sleeplessspecialist.peaktime.domain.chat.entity;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
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
 * 채팅 참여자(ChatParticipant) 엔티티
 * <p>
 * 각 참여자는 채팅방 내에서의 역할(RoleInRoom)을 가지며,
 * 이를 통해 방 생성자(HOST), 일반 참여자(GUEST) 등
 * 비즈니스 규칙을 명확하게 표현한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Entity
@Getter
@Table(name = "chat_participant")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ChatParticipant extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "chat_room_id")
	private ChatRoom chatRoom;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Column(nullable = false)
	@Enumerated(EnumType.STRING)
	private RoleInRoom roleInRoom;

	@Builder
	public ChatParticipant(ChatRoom chatRoom, User user, RoleInRoom roleInRoom) {
		this.chatRoom = chatRoom;
		this.user = user;
		this.roleInRoom = roleInRoom;
	}
}
