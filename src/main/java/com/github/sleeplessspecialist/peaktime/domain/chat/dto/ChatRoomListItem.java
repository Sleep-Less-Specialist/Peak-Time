package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GetChatRoomListRes 의 부분 List 요소
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ChatRoomListItem {

	private Long roomId;
	private ChatRoomStatus status;
	private String description;
	private LocalDateTime createdAt;
}
