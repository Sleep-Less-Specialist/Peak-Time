package com.github.sleeplessspecialist.peaktime.domain.chat.service;

import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.ChatMessageReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatParticipant;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.RoleInRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.exception.ChatErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatMessageRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatParticipantRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatRoomRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import lombok.RequiredArgsConstructor;

/**
 * .
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */

@Service
@RequiredArgsConstructor
public class ChatService {

	private final ChatRoomRepository chatRoomRepository;

	private final ChatParticipantRepository chatParticipantRepository;

	private final UserRepository userRepository;

	private final ChatMessageRepository chatMessageRepository;

	/**
	 * 클라이언트가 메시지를 발송했을때 서버에서 받은 메시지를 DB 에 저장하는
	 * 1. 채팅방이 존재하는지 검증
	 * 2. user 가 존재하는지 검증
	 * 3. 메시지 저장하기
	 *  isRead - false (API 호출후 읽음 상태로 전환)
	 */
	public void saveMessage(Long roomId, ChatMessageReq chatMessageReq) {

		ChatRoom chatRoom = chatRoomRepository.findById(roomId).orElseThrow(
			() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		User user = userRepository.findByEmail(chatMessageReq.getEmail()).orElseThrow(
			() -> new CustomException(ChatErrorCode.USER_NOT_FOUND));

		ChatMessage chatMessage = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(user)
			.content(chatMessageReq.getMessage())
			.build();
		chatMessageRepository.save(chatMessage);
	}

	/**
	 * 방생성 내부 로직
	 * 인증, 인가 로직 구현후 추가 구현 필요
	 * 1. user 가 존재하는지 검증
	 * 2. user 가 지식공유자인지, 아닌지 검증
	 * 3. 채팅방 생성후 저장
	 *  기본 상태 - Open
	 * 4. 방 생성자를 참여자로 등록
	 *  roleInRoom 상태 - Host
	 */

	public CreateChatRoomRes createRoom(Long userId, CreateChatRoomReq createChatRoomReq) {

		User user = userRepository.findById(userId).orElseThrow(
			() -> new CustomException(ChatErrorCode.USER_NOT_FOUND));

		ChatRoom chatRoom = ChatRoom.builder()
			.description(createChatRoomReq.getDescription())
			.build();
		chatRoomRepository.save(chatRoom);

		ChatParticipant chatParticipant = ChatParticipant.builder()
			.chatRoom(chatRoom)
			.user(user)
			.roleInRoom(RoleInRoom.HOST)
			.build();
		chatParticipantRepository.save(chatParticipant);

		return CreateChatRoomRes.builder()
			.roomId(chatRoom.getId())
			.userId(userId)
			.chatRoomStatus(chatRoom.getChatRoomStatus())
			.description(chatRoom.getDescription())
			.createdAt(chatRoom.getCreatedAt())
			.updatedAt(chatRoom.getUpdatedAt())
			.build();
	}
}

