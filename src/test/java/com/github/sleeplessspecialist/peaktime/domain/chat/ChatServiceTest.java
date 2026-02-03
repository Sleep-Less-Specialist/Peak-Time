package com.github.sleeplessspecialist.peaktime.domain.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

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
import com.github.sleeplessspecialist.peaktime.domain.chat.service.ChatService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * {@link ChatService}에 대한 단위 테스트 클래스입니다.
 *
 * <p>
 * 채팅 메시지 저장, 채팅방 생성/목록 조회, 참가 처리, 채팅방 종료 등
 * 채팅 도메인의 핵심 유스케이스를 Mockito 기반으로 검증합니다.
 * </p>
 *
 * @author 주우재
 * @since 2026. 2. 3.
 */
@ExtendWith(MockitoExtension.class)
class ChatServiceTest {

	@InjectMocks
	private ChatService chatService;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Mock
	private ChatParticipantRepository chatParticipantRepository;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Test
	@DisplayName("메시지 저장 성공")
	void saveMessage_success() {
		// given
		Long roomId = 10L;
		String email = "test@test.com";
		String message = "hello";

		User user = User.createForSignup("tester", email, "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", 1L);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(userRepository.findByEmail(email)).thenReturn(Optional.of(user));

		ChatMessageReq request = new ChatMessageReq(message, email);

		// when
		chatService.saveMessage(roomId, request);

		// then
		ArgumentCaptor<ChatMessage> messageCaptor = ArgumentCaptor.forClass(ChatMessage.class);
		verify(chatMessageRepository).save(messageCaptor.capture());
		ChatMessage saved = messageCaptor.getValue();
		assertThat(saved.getChatRoom()).isEqualTo(chatRoom);
		assertThat(saved.getUser()).isEqualTo(user);
		assertThat(saved.getContent()).isEqualTo(message);
		assertThat(ReflectionTestUtils.getField(saved, "isRead")).isEqualTo(false);
	}

	@Test
	@DisplayName("메시지 저장 실패 - 채팅방이 없으면 예외")
	void saveMessage_roomNotFound() {
		// given
		Long roomId = 10L;
		ChatMessageReq request = new ChatMessageReq("hello", "test@test.com");

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> chatService.saveMessage(roomId, request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		verify(userRepository, never()).findByEmail(anyString());
		verify(chatMessageRepository, never()).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("메시지 저장 실패 - 사용자가 없으면 예외")
	void saveMessage_userNotFound() {
		// given
		Long roomId = 10L;
		String email = "test@test.com";
		ChatMessageReq request = new ChatMessageReq("hello", email);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(userRepository.findByEmail(email)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> chatService.saveMessage(roomId, request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.USER_NOT_FOUND));

		verify(chatMessageRepository, never()).save(any(ChatMessage.class));
	}

	@Test
	@DisplayName("채팅방 생성 성공")
	void createRoom_success() {
		// given
		Long userId = 1L;
		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		CreateChatRoomReq request = new CreateChatRoomReq("room-desc");

		LocalDateTime createdAt = LocalDateTime.of(2026, 2, 3, 10, 0);
		LocalDateTime updatedAt = LocalDateTime.of(2026, 2, 3, 10, 5);

		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(chatRoomRepository.save(any(ChatRoom.class))).thenAnswer(invocation -> {
			ChatRoom room = invocation.getArgument(0);
			ReflectionTestUtils.setField(room, "id", 100L);
			ReflectionTestUtils.setField(room, "createdAt", createdAt);
			ReflectionTestUtils.setField(room, "updatedAt", updatedAt);
			return room;
		});

		// when
		CreateChatRoomRes response = chatService.createRoom(userId, request);

		// then
		ArgumentCaptor<ChatParticipant> participantCaptor = ArgumentCaptor.forClass(ChatParticipant.class);
		verify(chatParticipantRepository).save(participantCaptor.capture());
		ChatParticipant participant = participantCaptor.getValue();
		assertThat(participant.getUser()).isEqualTo(user);
		assertThat(participant.getRoleInRoom()).isEqualTo(RoleInRoom.HOST);

		assertThat(response.getRoomId()).isEqualTo(100L);
		assertThat(response.getUserId()).isEqualTo(userId);
		assertThat(response.getChatRoomStatus()).isEqualTo(ChatRoomStatus.OPEN);
		assertThat(response.getDescription()).isEqualTo("room-desc");
		assertThat(response.getCreatedAt()).isEqualTo(createdAt);
		assertThat(response.getUpdatedAt()).isEqualTo(updatedAt);
	}

	@Test
	@DisplayName("채팅방 생성 실패 - 사용자가 없으면 예외")
	void createRoom_userNotFound() {
		// given
		Long userId = 1L;
		CreateChatRoomReq request = new CreateChatRoomReq("room-desc");

		when(userRepository.findById(userId)).thenReturn(Optional.empty());

		// when / then
		assertThatThrownBy(() -> chatService.createRoom(userId, request))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.USER_NOT_FOUND));

		verify(chatRoomRepository, never()).save(any(ChatRoom.class));
		verify(chatParticipantRepository, never()).save(any(ChatParticipant.class));
	}

	@Test
	@DisplayName("채팅방 목록 조회 성공")
	void getChatRooms_success() {
		// given
		ChatRoom room1 = ChatRoom.builder()
			.description("room-1")
			.build();
		ReflectionTestUtils.setField(room1, "id", 101L);
		ReflectionTestUtils.setField(room1, "createdAt", LocalDateTime.of(2026, 2, 3, 9, 0));

		ChatRoom room2 = ChatRoom.builder()
			.description("room-2")
			.build();
		ReflectionTestUtils.setField(room2, "id", 102L);
		ReflectionTestUtils.setField(room2, "createdAt", LocalDateTime.of(2026, 2, 3, 10, 0));

		PageRequest pageable = PageRequest.of(0, 2);
		Page<ChatRoom> page = new PageImpl<>(List.of(room2, room1), pageable, 4);

		when(chatRoomRepository.findByChatRoomStatusNot(eq(ChatRoomStatus.CLOSED), any(Pageable.class)))
			.thenReturn(page);

		// when
		GetChatRoomListRes response = chatService.getChatRooms(1, 2);

		// then
		assertThat(response.getRooms()).hasSize(2);

		ChatRoomListItem item1 = response.getRooms().get(0);
		assertThat(item1.getRoomId()).isEqualTo(102L);
		assertThat(item1.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
		assertThat(item1.getDescription()).isEqualTo("room-2");
		assertThat(item1.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 10, 0));

		ChatRoomListItem item2 = response.getRooms().get(1);
		assertThat(item2.getRoomId()).isEqualTo(101L);
		assertThat(item2.getStatus()).isEqualTo(ChatRoomStatus.OPEN);
		assertThat(item2.getDescription()).isEqualTo("room-1");
		assertThat(item2.getCreatedAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 9, 0));

		assertThat(response.getPage()).isEqualTo(0);
		assertThat(response.getSize()).isEqualTo(2);
		assertThat(response.getTotalElements()).isEqualTo(4);
		assertThat(response.getTotalPages()).isEqualTo(2);
		assertThat(response.isHasNext()).isTrue();

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(chatRoomRepository).findByChatRoomStatusNot(eq(ChatRoomStatus.CLOSED), pageableCaptor.capture());
		Pageable captured = pageableCaptor.getValue();
		assertThat(captured.getPageNumber()).isEqualTo(0);
		assertThat(captured.getPageSize()).isEqualTo(2);
		assertThat(captured.getSort().getOrderFor("createdAt").getDirection().isDescending()).isTrue();
	}

	@Test
	@DisplayName("채팅방 참가 성공 - 참가자 추가 및 MATCHED 상태로 전환")
	void addParticipantToChat_success() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(chatParticipantRepository.countByChatRoomId(roomId)).thenReturn(1L);
		when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, user)).thenReturn(false);

		// when
		chatService.addParticipantToChat(userId, roomId);

		// then
		ArgumentCaptor<ChatParticipant> participantCaptor = ArgumentCaptor.forClass(ChatParticipant.class);
		verify(chatParticipantRepository).save(participantCaptor.capture());
		ChatParticipant participant = participantCaptor.getValue();
		assertThat(participant.getUser()).isEqualTo(user);
		assertThat(participant.getRoleInRoom()).isEqualTo(RoleInRoom.GUEST);

		assertThat(chatRoom.getChatRoomStatus()).isEqualTo(ChatRoomStatus.MATCHED);
	}

	@Test
	@DisplayName("채팅방 참가 실패 - 채팅방이 OPEN 상태가 아니면 예외")
	void addParticipantToChat_roomNotOpen() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);
		ReflectionTestUtils.setField(chatRoom, "chatRoomStatus", ChatRoomStatus.CLOSED);

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(chatParticipantRepository.countByChatRoomId(roomId)).thenReturn(0L);

		// when / then
		assertThatThrownBy(() -> chatService.addParticipantToChat(userId, roomId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_OPEN));

		verify(chatParticipantRepository, never()).save(any(ChatParticipant.class));
		verify(chatParticipantRepository, never()).existsByChatRoomAndUser(any(ChatRoom.class), any(User.class));
	}

	@Test
	@DisplayName("채팅방 참가 실패 - 이미 참여 중인 사용자는 예외")
	void addParticipantToChat_alreadyParticipant() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		User user = User.createForSignup("tester", "test@test.com", "encoded", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(userRepository.findById(userId)).thenReturn(Optional.of(user));
		when(chatParticipantRepository.countByChatRoomId(roomId)).thenReturn(1L);
		when(chatParticipantRepository.existsByChatRoomAndUser(chatRoom, user)).thenReturn(true);

		// when / then
		assertThatThrownBy(() -> chatService.addParticipantToChat(userId, roomId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.ALREADY_PARTICIPANT));

		verify(chatParticipantRepository, never()).save(any(ChatParticipant.class));
	}

	@Test
	@DisplayName("채팅방 종료 성공 - 방장 권한으로 종료 처리")
	void closeRoom_success() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(chatParticipantRepository.existsByChatRoomIdAndUserIdAndRoleInRoom(
			roomId, userId, RoleInRoom.HOST)).thenReturn(true);

		// when
		chatService.closeRoom(roomId, userId);

		// then
		assertThat(chatRoom.getChatRoomStatus()).isEqualTo(ChatRoomStatus.CLOSED);
		assertThat(ReflectionTestUtils.getField(chatRoom, "closedAt")).isNotNull();
	}

	@Test
	@DisplayName("채팅방 종료 실패 - 사용자가 없으면 예외")
	void closeRoom_userNotFound() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		when(userRepository.existsById(userId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> chatService.closeRoom(roomId, userId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.USER_NOT_FOUND));

		verify(chatRoomRepository, never()).findById(anyLong());
	}

	@Test
	@DisplayName("채팅방 종료 실패 - 방장 권한이 없으면 예외")
	void closeRoom_unauthorized() {
		// given
		Long roomId = 10L;
		Long userId = 1L;

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.findById(roomId)).thenReturn(Optional.of(chatRoom));
		when(chatParticipantRepository.existsByChatRoomIdAndUserIdAndRoleInRoom(
			roomId, userId, RoleInRoom.HOST)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> chatService.closeRoom(roomId, userId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.UNAUTHORIZED_ACCESS));

		assertThat(chatRoom.getChatRoomStatus()).isEqualTo(ChatRoomStatus.OPEN);
	}
}
