package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성 응답 dto
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class CreateChatRoomRes {

	private Long roomId;
	private Long userId;
	private ChatRoomStatus chatRoomStatus;
	private String description;
	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;
}
