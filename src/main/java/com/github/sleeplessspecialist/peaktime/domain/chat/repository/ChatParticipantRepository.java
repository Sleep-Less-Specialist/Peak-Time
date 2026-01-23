package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatParticipant;

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
public interface ChatParticipantRepository extends JpaRepository<ChatParticipant, Long> {
}
