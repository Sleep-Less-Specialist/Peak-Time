package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import jakarta.validation.constraints.Size;
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

	@Size(max = 255, message = "채팅방 설명은 최대 255 까지 입력할 수 있습니다.")
	private String description;
}
