package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatParticipant;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

/**
 채팅방 참여자(ChatParticipant) 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스입니다.
 *
 * <p>
 * 채팅방 참가 여부 검증, 중복 참가 방지, 접근 권한 확인 등의 용도로 사용됩니다.
 * 주로 다음과 같은 시나리오에서 활용됩니다:
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {

	boolean existsByChatRoomAndUser(ChatRoom chatRoom, User user);

	boolean existsByChatRoomIdAndUserId(Long roomId, Long userId);

	long countByChatRoomId(Long roomId);
}
