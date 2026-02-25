package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import jakarta.persistence.LockModeType;
import jakarta.persistence.QueryHint;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoom;
import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatRoomStatus;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

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

    /**
     * 특정 채팅방(row)에 대해 비관적 쓰기 락(X-lock)을 획득합니다.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("select r from ChatRoom r where r.id = :roomId")
    @QueryHints(
            @QueryHint(name = "jakarta.persistence.lock.timeout", value = "3000")
    )
    Optional<ChatRoom> findByIdForUpdate(@Param("roomId") Long roomId);
}
