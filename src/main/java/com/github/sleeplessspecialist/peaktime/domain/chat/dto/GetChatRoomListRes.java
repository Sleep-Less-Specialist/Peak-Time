package com.github.sleeplessspecialist.peaktime.domain.chat.dto;

import java.util.List;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 커피쳇 방 목록 조회 응답 DTO입니다.
 *
 * <p>
 * 페이지네이션이 적용된 커피쳇 방 목록을 반환하며,
 * 각 페이지에 포함된 채팅방 정보와 함께 현재 페이지 번호,
 * 페이지 크기, 전체 데이터 개수 및 전체 페이지 수를 포함합니다.
 * </p>
 *
 * <p>
 * 정렬 기준은 요청 시 전달된 {@code Pageable} 정보를 기반으로 하며,
 * 기본적으로 생성일시(createdAt)를 기준으로 내림차순(DESC) 정렬됩니다.
 * 페이지 번호는 0부터 시작하는 0-base index를 사용합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.23
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class GetChatRoomListRes {

	private List<ChatRoomListItem> rooms;
	private int page;
	private int size;
	private long totalElements;
	private int totalPages;
	private boolean hasNext;
	private String sort;

}