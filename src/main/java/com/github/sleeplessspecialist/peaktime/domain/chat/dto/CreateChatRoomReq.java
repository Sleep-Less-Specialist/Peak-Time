package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import jakarta.validation.constraints.Size;
import lombok.*;

/**
 * 채팅방 생성 요청 dto
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@Getter
@RequiredArgsConstructor
@Builder
public class CreateChatRoomReq {

	@Size(max = 255)
	private final String description;
}
