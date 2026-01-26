package com.github.sleeplessspecialist.peaktime.domain.chat.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
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
import lombok.extern.slf4j.Slf4j;

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

@Slf4j
@Service
@RequiredArgsConstructor
public class ChatService {

	private static final int ROOM_CAPACITY = 2;

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

		User user = getUser(userId);

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
	 * 커피쳇 방 목록을 페이지네이션과 정렬 조건에 따라 조회
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
			.map(ChatRoomListItem::from)
			.toList();

		return GetChatRoomListRes.builder()
			.rooms(rooms)
			.page(roomPage.getNumber())
			.size(roomPage.getSize())
			.totalElements(roomPage.getTotalElements())
			.totalPages(roomPage.getTotalPages())
			.hasNext(roomPage.hasNext())
			.sort(toSortString(roomPage.getSort()))
			.build();
	}

	/**
	 * 커피쳇 참여
	 *
	 * <p>
	 *    1. 참여 하고자하는 roomId -> room 이 존재하는지 검증
	 * 	  2. 현재 인증 userId 가 DB 에 존재하는 user 인지 검증
	 * 	  3. 채팅방이 OPEN 상태인지 검증 (CLOSED, MATCHED) 이면 참여 불가능
	 * 	  	- 정책 에서 이미 MATCHED 라면 participant.size() = 2 임을 보장
	 * 	  4. chatRoom 에 연관된 participant 에 user 가 존재하는지 검증
	 * 	  	- 정책 에서 참여자 상태가 host 여야 하지만 room 이 현재 user 를 포함하는지 검증이 더 포괄적인 검증
	 * 	  5. 참가자 저장 + 상태 전이(MATCHED)
	 * </p>
	 *
	 */
	@Transactional
	public void addParticipantToChat(Long userId, Long roomId) {

		ChatRoom chatRoom = getChatRoom(roomId);
		User user = getUser(userId);

		validateJoinableRoom(chatRoom, roomId, userId);
		validateNotAlreadyParticipant(chatRoom, user, roomId, userId);

		ChatParticipant chatParticipant = ChatParticipant.builder()
			.chatRoom(chatRoom)
			.user(user)
			.roleInRoom(RoleInRoom.GUEST)
			.build();
		chatParticipantRepository.save(chatParticipant);

		chatRoom.matched();
	}

	/**
	 * 커피챗 종료하기
	 * 1. roomId DB 존재 여부 확인
	 * 2. userId DB 존재 여부 확인
	 * 3. 방의 참여자이면서 Host 인지 검증
	 * 4. 상태 전이(OPEN, MATCHED -> CLOSED)
	 */
	@Transactional
	public void closeRoom(Long roomId, Long userId) {

		validateUser(userId);

		ChatRoom chatRoom = getChatRoom(userId);

		validateHostPermission(roomId, userId);
		chatRoom.close();
	}

	/**
	 * Room 이 DB 에 존재 하는지 검증 + 없다면 throw
	 */
	private ChatRoom getChatRoom(Long roomId) {
		return chatRoomRepository.findById(roomId)
			.orElseThrow(() -> new CustomException(ChatErrorCode.CHAT_ROOM_NOT_FOUND));
	}

	/**
	 * User 가 DB 에 존재 하는지 검증 + 없다면 throw
	 */
	private User getUser(Long userId) {
		return userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(ChatErrorCode.USER_NOT_FOUND));
	}

	/**
	 * User 가 DB 에 존재 하는지 검증
	 */
	private void validateUser(Long userId) {
		if (!userRepository.existsById(userId)) {
			throw new CustomException(ChatErrorCode.USER_NOT_FOUND);
		}
	}

	/**
	 * 참가 가능한 방 상태인지 검증합니다. (OPEN만 허용)
	 * 방이 open 상태 임과 동시에 방 정원보다 작은지 검증
	 */
	private void validateJoinableRoom(ChatRoom chatRoom, Long roomId, Long userId) {

		long participantNum = chatParticipantRepository.countByChatRoomId(roomId);

		if (chatRoom.getChatRoomStatus() != ChatRoomStatus.OPEN || participantNum >= ROOM_CAPACITY
		) {
			log.warn("채팅방 참가 거절: 방이 OPEN 상태가 아닙니다 (현재 참여자: {}). roomId={}, status={}, userId={}",
				participantNum, roomId, chatRoom.getChatRoomStatus(), userId);

			throw new CustomException(ChatErrorCode.CHAT_ROOM_NOT_OPEN);
		}
	}

	/**
	 * 이미 참가자인지 검증합니다.
	 */
	private void validateNotAlreadyParticipant(ChatRoom chatRoom, User user, Long roomId, Long userId) {
		if (chatParticipantRepository.existsByChatRoomAndUser(chatRoom, user)) {
			log.warn("채팅방 참가 거절: 이미 참가한 사용자입니다. roomId={}, userId={}",
				roomId, userId);

			throw new CustomException(ChatErrorCode.ALREADY_PARTICIPANT);
		}
	}

	/**
	 * 참가자인지 검증과 동시에 RoleInRoom 이 RoleInRoom.HOST 인지 검증합니다.
	 */
	private void validateHostPermission(Long roomId, Long userId) {
		boolean isHost = chatParticipantRepository
			.existsByChatRoomIdAndUserIdAndRoleInRoom(roomId, userId, RoleInRoom.HOST);

		if (!isHost) {
			log.warn("채팅방 닫기 실패: roomId={}, userId={}", roomId, userId);
			throw new CustomException(ChatErrorCode.UNAUTHORIZED_ACCESS);
		}
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

	/**
	 * 정렬 정보를 문자열로 나타내는 메서드
	 */
	private String toSortString(Sort sort) {
		return sort.stream()
			.map(order -> order.getProperty() + "," + order.getDirection())
			.findFirst()
			.orElse(null);
	}

}

