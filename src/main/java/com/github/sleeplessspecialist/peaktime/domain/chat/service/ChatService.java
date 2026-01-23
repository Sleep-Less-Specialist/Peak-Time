package com.github.sleeplessspecialist.peaktime.domain.chat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.ChatMessageReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.ChatRoomListItem;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetChatRoomListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatParticipant;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;
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
 * 커피쳇(Chat) 도메인의 핵심 비즈니스 로직을 처리하는 서비스 클래스입니다.
 *  *
 *  <p>
 *
 *  </p>
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
	@Transactional
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

	@Transactional
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

	/**
	 * 커피쳇 방 목록을 페이지네이션과 정렬 조건에 따라 조회합니다.
	 *
	 * <p>
	 * CLOSED 상태가 아닌 커피쳇 방을 대상으로 하며,
	 * 요청된 {@link Pageable} 정보(page, size, sort)를 기준으로
	 * 목록과 함께 페이지 메타데이터를 반환합니다.
	 * </p>
	 *
	 * <p>
	 * 조회 결과에는 현재 페이지 번호, 페이지 크기,
	 * 전체 데이터 수, 전체 페이지 수, 다음 페이지 존재 여부 및
	 * 적용된 정렬 기준이 포함됩니다.
	 * </p>
	 *
	 * @param pageable 페이지 번호, 페이지 크기, 정렬 기준 정보
	 * @return 페이지네이션이 적용된 커피쳇 방 목록 응답
	 */
	@Transactional(readOnly = true)
	public GetChatRoomListRes getChatRooms(Pageable pageable) {

		validatePageable(pageable);

		Page<ChatRoom> roomPage =
			chatRoomRepository.findByChatRoomStatusNot(ChatRoomStatus.CLOSED, pageable);

		List<ChatRoomListItem> rooms = roomPage.getContent().stream()
			.map(room -> ChatRoomListItem.builder()
				.roomId(room.getId())
				.status(room.getChatRoomStatus())
				.description(room.getDescription())
				.createdAt(room.getCreatedAt())
				.build()
			)
			.toList();

		return GetChatRoomListRes.builder()
			.rooms(rooms)
			.page(roomPage.getNumber())
			.size(roomPage.getSize())
			.totalElements(roomPage.getTotalElements())
			.totalPages(roomPage.getTotalPages())
			.hasNext(roomPage.hasNext())
			.sort(roomPage.getSort().stream()
				.map(order -> order.getProperty() + "," + order.getDirection())
				.findFirst()
				.orElse(null))
			.build();
	}

	/**
	 * 페이지 쿼리 파라미터 변수를 검증하는 메서드
	 * 1. page > 0
	 * 2. size >= 0 or size < 50 (정책)
	 */
	private void validatePageable(Pageable pageable) {

		int page = pageable.getPageNumber();
		int size = pageable.getPageSize();

		if (page < 0) {
			throw new CustomException(ChatErrorCode.BAD_PAGING_CONDITION);
		}

		if (size < 1 || size > 50) {
			throw new CustomException(ChatErrorCode.BAD_PAGING_CONDITION);
		}

	}
}

