package com.github.sleeplessspecialist.peaktime.domain.chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetMessageListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.MessageService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import lombok.RequiredArgsConstructor;

/**
 * 채팅방 메시지 조회 API를 제공하는 컨트롤러 클래스입니다.
 * <p>
 * 클라이언트로부터 채팅방 ID와 커서(lastId), 페이지 크기(size)를 전달받아
 * 커서 기반 페이징 방식으로 메시지 목록을 조회하고, 표준 응답 형식({@link ApiResponse})으로 반환합니다.
 * </p>
 *
 * <p>
 * 실제 메시지 조회 로직은 {@link MessageService}에서 처리합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 1. 26.
 */
@RestController
@RequestMapping("/api/v1/chat")
@RequiredArgsConstructor
public class MessageController {

	private final MessageService messageService;

	@GetMapping("/room/{roomId}/messages")
	public ApiResponse<GetMessageListRes> getMessages(
		@PathVariable("roomId") Long roomId,
		@RequestParam(required = false) Long lastId,
		@RequestParam(required = false) int size
	) {
		Long userId = 1L;
		GetMessageListRes response = messageService.findMessagePageByCursor(userId, roomId, lastId, size);
		return ApiResponse.of(SuccessCode.OK, response);
	}
}
