package com.github.sleeplessspecialist.peaktime.domain.chat.dto;
import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 채팅방 메시지 목록 조회 응답 dto
 * <p>
 * cursor-based-pagination 사용
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2025. 12. 22.
 */
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetMessageListRes {

	private Long chatRoomId;
	private int size;
	private boolean hasNext;
	private Long nextCursor;
	private List<ChatMessageListItem> messageList;
}