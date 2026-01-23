package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;

/**
 * .
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
public interface ChatRoomRepository extends JpaRepository<ChatRoom, Long> {

	/**
	 * chatRoomStatus 를 제외한 상태 조회
	 */
	Page<ChatRoom> findByChatRoomStatusNot(ChatRoomStatus chatRoomStatus, Pageable pageable);
}
