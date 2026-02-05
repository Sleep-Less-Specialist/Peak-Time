package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 생성 응답 dto
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateChatRoomRes {

	private final Long roomId;
	private final Long userId;
	private final ChatRoomStatus chatRoomStatus;
	private final String description;
	private final LocalDateTime createdAt;
	private final LocalDateTime updatedAt;
}
