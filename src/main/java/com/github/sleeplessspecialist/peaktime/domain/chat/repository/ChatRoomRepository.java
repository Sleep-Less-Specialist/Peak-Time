package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;

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
}
