package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 생성 요청 dto
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class CreateChatRoomReq {

	private String description;
}
