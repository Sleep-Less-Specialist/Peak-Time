package com.github.sleeplessspecialist.peaktime.domain.chat.service;

import java.util.List;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.ChatMessageListItem;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetMessageListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;
import com.github.sleeplessspecialist.peaktime.domain.chat.exception.ChatErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatMessageRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatParticipantRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatRoomRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 채팅방 메시지 조회 관련 비즈니스 로직을 처리하는 서비스 클래스입니다.
 * <p>
 * 커서 기반 페이징 방식을 사용하여 특정 채팅방의 메시지를 페이지 단위로 조회하며,
 * 요청 파라미터에 대한 유효성 검증, 사용자 존재 여부 검증, 채팅방 접근 권한 검증을 수행합니다.
 * </p>
 *
 * <p>
 * 조회된 메시지는 {@link ChatMessageListItem} DTO로 변환되어 반환되며,
 * 다음 페이지 조회를 위한 커서(nextCursor) 정보와 함께 응답 객체 {@link GetMessageListRes}로 구성됩니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 25.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class MessageService {

	private final UserRepository userRepository;

	private final ChatParticipantRepository chatParticipantRepository;

	private final ChatMessageRepository chatMessageRepository;
	private final ChatRoomRepository chatRoomRepository;

	/**
	 * 1. userId 유효성 검사
	 * 2. roomId 유효성 검사
	 * 3. user 의 room 에 대한 권한 검사
	 * 4. 응답 dto 리턴
	 */
	@Transactional(readOnly = true)
	public GetMessageListRes findMessagePageByCursor(Long userId, Long roomId, Long lastId, int size) {

		validateUser(userId);

		validateRoom(roomId);

		validateAccess(roomId, userId);

		Pageable pageable = PageRequest.of(0, size);

		Slice<ChatMessage> messageSlice = chatMessageRepository.findMessagesByCursor(roomId, lastId, pageable);

		List<ChatMessage> content = messageSlice.getContent();

		List<ChatMessageListItem> messages = content.stream()
			.map(ChatMessageListItem::from)
			.toList();

		Long nextCursor = calculateNextCursor(content, messageSlice.hasNext());

		return GetMessageListRes.builder()
			.chatRoomId(roomId)
			.size(size)
			.hasNext(messageSlice.hasNext())
			.nextCursor(nextCursor)
			.messageList(messages)
			.build();
	}

	/**
	 * 1. user 유효성 검사
	 * 2. room 권한 검사
	 * 3. user 의 room 에 대한 권한 검사
	 * 4. roomId의 모든 메시지에서 isRead = True
	 */
	@Transactional
	public void readAllMessages(Long userId, Long roomId) {

		validateUser(userId);

		validateRoom(roomId);

		validateAccess(roomId, userId);

		chatMessageRepository.bulkReadAll(roomId);
	}

	private void validateUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			log.warn(" 존재하지 않는 사용자입니다. userId={}", userId);
			throw new CustomException(ChatErrorCode.USER_NOT_FOUND);
		}
	}

	private void validateRoom(Long roomId) {
		if (!chatRoomRepository.existsById(roomId)) {
			log.warn(" 존재하지 않는 채팅방입니다. roomId={}", roomId);
			throw new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND);
		}
	}

	private void validateAccess(Long roomId, Long userId) {
		boolean isParticipant = chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId);
		if (!isParticipant) {
			log.warn(
				"채팅방 접근 권한이 없습니다. roomId={}, userId={}",
				roomId, userId
			);
			throw new CustomException(ChatErrorCode.UNAUTHORIZED_ACCESS);
		}
	}


	/**
	 * 다음 페이지 커서(nextCursor) 계산
	 */
	private Long calculateNextCursor(List<ChatMessage> content, boolean hasNext) {
		if (!hasNext || content.isEmpty()) {
			return null;
		}
		return content.get(content.size() - 1).getId();
	}
}
