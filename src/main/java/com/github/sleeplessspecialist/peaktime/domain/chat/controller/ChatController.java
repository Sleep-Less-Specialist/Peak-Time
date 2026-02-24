package com.github.sleeplessspecialist.peaktime.domain.chat.controller;

import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetChatRoomListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.ChatService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 채팅(Chat) 관련 API를 제공하는 컨트롤러
 *
 * <p>
 * 채팅방 생성, 참가, 메시지 송수신 등
 * 채팅 도메인의 진입점 역할을 한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.22
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class ChatController {

	private final ChatService chatService;

	/**
	 * 채팅방 생성 API
	 */
	@PostMapping("/room")
	public ApiResponse<CreateChatRoomRes> createRoom(
		@AuthenticationPrincipal Long userId,
		@RequestBody @Valid CreateChatRoomReq createChatRoomReq) {

		CreateChatRoomRes response = chatService.createRoom(userId, createChatRoomReq);
		return ApiResponse.of(SuccessCode.CREATED, response);
	}

	/**
	 * 채팅방 전체 조회 API
	 * 인증 필요 X
	 */
	@GetMapping("/room")
	public ApiResponse<GetChatRoomListRes> getRooms(
		@RequestParam(defaultValue = "1") @Min(1) int page,
		@RequestParam(defaultValue = "10") @Min(1) @Max(50) int size) {

		GetChatRoomListRes response = chatService.getChatRooms(page, size);
		return ApiResponse.of(SuccessCode.OK, response);
	}

	/**
	 * 채팅방 참여 API
	 */
	@PostMapping("/room/{roomId}/join")
	public ApiResponse<Void> joinRoom(
		@AuthenticationPrincipal Long userId,
		@PathVariable @Positive Long roomId) {

		chatService.addParticipantToChatWithPessimisticLock(userId, roomId);
		return ApiResponse.ok();
	}

	/**
	 * 커피챗 종료 API
	 */
	@PostMapping("/room/{roomId}/close")
	public ApiResponse<Void> closeRoom(
		@AuthenticationPrincipal Long userId,
		@PathVariable @Positive Long roomId) {

		chatService.closeRoom(roomId, userId);
		return ApiResponse.ok();
	}
}
