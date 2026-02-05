package com.github.sleeplessspecialist.peaktime.domain.chat;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.domain.SliceImpl;
import org.springframework.test.util.ReflectionTestUtils;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.ChatMessageListItem;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetMessageListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.exception.ChatErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatMessageRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatParticipantRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.repository.ChatRoomRepository;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.MessageService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

/**
 * {@link MessageService}에 대한 단위 테스트 클래스입니다.
 *
 * <p>
 * 메시지 커서 기반 조회, 읽음 처리(전체 읽음) 등 메시지 관련 핵심 비즈니스 로직을
 * Mockito 기반으로 검증합니다.
 * </p>
 *
 * @author 주우재
 * @since 2026. 2. 3.
 */
@ExtendWith(MockitoExtension.class)
class MessageServiceTest {

	@InjectMocks
	private MessageService messageService;

	@Mock
	private UserRepository userRepository;

	@Mock
	private ChatParticipantRepository chatParticipantRepository;

	@Mock
	private ChatMessageRepository chatMessageRepository;

	@Mock
	private ChatRoomRepository chatRoomRepository;

	@Test
	@DisplayName("메시지 커서 기반 조회 성공")
	void findMessagePageByCursor_success() {
		// given
		Long userId = 1L;
		Long roomId = 10L;
		Long lastId = 99L;
		int size = 2;

		User user = User.createForSignup("tester", "test@test.com", "pw", "010-0000-0000");
		ReflectionTestUtils.setField(user, "id", userId);

		ChatRoom chatRoom = ChatRoom.builder()
			.description("room")
			.build();
		ReflectionTestUtils.setField(chatRoom, "id", roomId);

		ChatMessage message1 = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(user)
			.content("hello-1")
			.build();
		ReflectionTestUtils.setField(message1, "id", 30L);
		ReflectionTestUtils.setField(message1, "createdAt", LocalDateTime.of(2026, 2, 3, 10, 0));
		ReflectionTestUtils.setField(message1, "isRead", true);

		ChatMessage message2 = ChatMessage.builder()
			.chatRoom(chatRoom)
			.user(user)
			.content("hello-2")
			.build();
		ReflectionTestUtils.setField(message2, "id", 20L);
		ReflectionTestUtils.setField(message2, "createdAt", LocalDateTime.of(2026, 2, 3, 9, 50));

		List<ChatMessage> content = List.of(message1, message2);
		Slice<ChatMessage> slice = new SliceImpl<>(content, PageRequest.of(0, size), true);

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(true);
		when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
		when(chatMessageRepository.findMessagesByCursor(eq(roomId), eq(lastId), any(Pageable.class)))
			.thenReturn(slice);

		// when
		GetMessageListRes response = messageService.findMessagePageByCursor(userId, roomId, lastId, size);

		// then
		assertThat(response.getChatRoomId()).isEqualTo(roomId);
		assertThat(response.getSize()).isEqualTo(size);
		assertThat(response.isHasNext()).isTrue();
		assertThat(response.getNextCursor()).isEqualTo(20L);
		assertThat(response.getMessageList()).hasSize(2);

		ChatMessageListItem item1 = response.getMessageList().get(0);
		assertThat(item1.getMessageId()).isEqualTo(30L);
		assertThat(item1.getSenderId()).isEqualTo(userId);
		assertThat(item1.getSenderName()).isEqualTo("tester");
		assertThat(item1.getContent()).isEqualTo("hello-1");
		assertThat(item1.isRead()).isTrue();
		assertThat(item1.getCreateAt()).isEqualTo(LocalDateTime.of(2026, 2, 3, 10, 0));

		ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
		verify(chatMessageRepository).findMessagesByCursor(eq(roomId), eq(lastId), pageableCaptor.capture());
		Pageable captured = pageableCaptor.getValue();
		assertThat(captured.getPageNumber()).isEqualTo(0);
		assertThat(captured.getPageSize()).isEqualTo(size);
	}

	@Test
	@DisplayName("메시지 커서 기반 조회 - 다음 페이지가 없으면 nextCursor는 null")
	void findMessagePageByCursor_nextCursorNull_whenNoNext() {
		// given
		Long userId = 1L;
		Long roomId = 10L;
		Long lastId = null;
		int size = 2;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(true);
		when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);
		when(chatMessageRepository.findMessagesByCursor(eq(roomId), isNull(), any(Pageable.class)))
			.thenReturn(new SliceImpl<>(List.of(), PageRequest.of(0, size), false));

		// when
		GetMessageListRes response = messageService.findMessagePageByCursor(userId, roomId, lastId, size);

		// then
		assertThat(response.isHasNext()).isFalse();
		assertThat(response.getNextCursor()).isNull();
		assertThat(response.getMessageList()).isEmpty();
	}

	@Test
	@DisplayName("메시지 커서 기반 조회 실패 - 사용자 없음")
	void findMessagePageByCursor_userNotFound() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.findMessagePageByCursor(userId, roomId, null, 10))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.USER_NOT_FOUND));

		verify(chatRoomRepository, never()).existsById(anyLong());
		verify(chatParticipantRepository, never()).existsByChatRoomIdAndUserId(anyLong(), anyLong());
		verify(chatMessageRepository, never()).findMessagesByCursor(anyLong(), any(), any(Pageable.class));
	}

	@Test
	@DisplayName("메시지 커서 기반 조회 실패 - 채팅방 없음")
	void findMessagePageByCursor_roomNotFound() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.findMessagePageByCursor(userId, roomId, null, 10))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		verify(chatParticipantRepository, never()).existsByChatRoomIdAndUserId(anyLong(), anyLong());
		verify(chatMessageRepository, never()).findMessagesByCursor(anyLong(), any(), any(Pageable.class));
	}

	@Test
	@DisplayName("메시지 커서 기반 조회 실패 - 채팅방 접근 권한 없음")
	void findMessagePageByCursor_unauthorized() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(true);
		when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.findMessagePageByCursor(userId, roomId, null, 10))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.UNAUTHORIZED_ACCESS));

		verify(chatMessageRepository, never()).findMessagesByCursor(anyLong(), any(), any(Pageable.class));
	}

	@Test
	@DisplayName("전체 메시지 읽음 처리 성공")
	void readAllMessages_success() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(true);
		when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(true);

		// when
		messageService.readAllMessages(userId, roomId);

		// then
		verify(chatMessageRepository).bulkReadAll(roomId);
	}

	@Test
	@DisplayName("전체 메시지 읽음 처리 실패 - 사용자 없음")
	void readAllMessages_userNotFound() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.readAllMessages(userId, roomId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.USER_NOT_FOUND));

		verify(chatRoomRepository, never()).existsById(anyLong());
		verify(chatParticipantRepository, never()).existsByChatRoomIdAndUserId(anyLong(), anyLong());
		verify(chatMessageRepository, never()).bulkReadAll(anyLong());
	}

	@Test
	@DisplayName("전체 메시지 읽음 처리 실패 - 채팅방 없음")
	void readAllMessages_roomNotFound() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.readAllMessages(userId, roomId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.CHAT_ROOM_NOT_FOUND));

		verify(chatParticipantRepository, never()).existsByChatRoomIdAndUserId(anyLong(), anyLong());
		verify(chatMessageRepository, never()).bulkReadAll(anyLong());
	}

	@Test
	@DisplayName("전체 메시지 읽음 처리 실패 - 채팅방 접근 권한 없음")
	void readAllMessages_unauthorized() {
		// given
		Long userId = 1L;
		Long roomId = 10L;

		when(userRepository.existsById(userId)).thenReturn(true);
		when(chatRoomRepository.existsById(roomId)).thenReturn(true);
		when(chatParticipantRepository.existsByChatRoomIdAndUserId(roomId, userId)).thenReturn(false);

		// when / then
		assertThatThrownBy(() -> messageService.readAllMessages(userId, roomId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> assertThat(((CustomException) ex).getErrorCode())
				.isEqualTo(ChatErrorCode.UNAUTHORIZED_ACCESS));

		verify(chatMessageRepository, never()).bulkReadAll(anyLong());
	}
}
