package com.github.sleeplessspecialist.peaktime.domain.chat.repository;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Slice;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sleeplessspecialist.peaktime.domain.chat.entity.ChatMessage;

/**
 * 채팅 메시지 엔티티({@link ChatMessage})에 대한 데이터 접근을 담당하는 리포지토리 인터페이스입니다.
 * <p>
 * 커서 기반 페이징 방식을 사용하여 특정 채팅방의 메시지를 효율적으로 조회하는 기능을 제공합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 26.
 */

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

	/**
	 * 채팅방의 메시지를 커서 기반으로 조회합니다.
	 * <p>
	 *  id 기준 내림차순(DESC) 정렬
	 * 	lastId가 null이면 최신 메시지부터 조회
	 * 	lastId가 있으면 해당 id보다 작은 메시지만 조회 (다음 페이지)
	 * 	무한 스크롤용 Slice 페이징 지원
	 * </p>
	 */
	@Query("""
    SELECT m
    FROM ChatMessage m JOIN FETCH m.user
    WHERE m.chatRoom.id = :roomId
      AND (:lastId IS NULL OR m.id < :lastId)
    ORDER BY m.id DESC""")
	Slice<ChatMessage> findMessagesByCursor(
		@Param("roomId") Long roomId,
		@Param("lastId") Long lastId,
		Pageable pageable
	);
}
