package com.github.sleeplessspecialist.peaktime.domain.chat.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomReq;
import com.github.sleeplessspecialist.peaktime.domain.chat.dto.CreateChatRoomRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.ChatService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.Valid;
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
	 * 인증, 인가 미구현으로 임시 하드코딩
	 */
	@PostMapping("/room")
	public ApiResponse<CreateChatRoomRes> createRoom(@RequestBody @Valid CreateChatRoomReq createChatRoomReq){

		Long userId = 1L;
		CreateChatRoomRes response = chatService.createRoom(userId, createChatRoomReq);
		return ApiResponse.of(SuccessCode.CREATED,response);
	}
}
