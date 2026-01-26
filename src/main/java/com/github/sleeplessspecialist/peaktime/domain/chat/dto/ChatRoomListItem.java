package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.RequiredArgsConstructor;

/**
 * GetChatRoomListRes 의 부분 List 요소
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@RequiredArgsConstructor
@Builder
public class ChatRoomListItem {

	private final Long roomId;
	private final ChatRoomStatus status;
	private final String description;
	private final LocalDateTime createdAt;

	public static ChatRoomListItem from(ChatRoom chatRoom) {
		return ChatRoomListItem.builder()
			.roomId(chatRoom.getId())
			.status(chatRoom.getChatRoomStatus())
			.description(chatRoom.getDescription())
			.createdAt(chatRoom.getCreatedAt())
			.build();
	}
}
