package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

/**
 * 채팅방 메시지 목록 조회 시 사용되는 응답용 DTO 클래스입니다.
 * <p>
 * {@link ChatMessage} 엔티티의 주요 정보를 클라이언트에 전달하기 위한 데이터 전송 객체로,
 * 메시지 식별자, 발신자 정보, 메시지 내용, 읽음 여부 및 생성 시각을 포함합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 26.
 */

import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
@Builder
public class ChatMessageListItem {

	private final Long messageId;
	private final Long senderId;
	private final String senderName;
	private final String content;
	private final boolean isRead;
	private final LocalDateTime createAt;

	public static ChatMessageListItem from(ChatMessage message) {
		return ChatMessageListItem.builder()
			.messageId(message.getId())
			.senderId(message.getUser().getId())
			.senderName(message.getUser().getName())
			.content(message.getContent())
			.isRead(message.isRead())
			.createAt(message.getCreatedAt())
			.build();
	}
}

