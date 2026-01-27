package com.github.sleeplessspecialist.peaktime.domain.chat.controller;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import com.github.sleeplessspecialist.peaktime.domain.chat.dto.GetMessageListRes;
import com.github.sleeplessspecialist.peaktime.domain.chat.service.MessageService;
import com.github.sleeplessspecialist.peaktime.global.common.response.ApiResponse;
import com.github.sleeplessspecialist.peaktime.global.common.response.SuccessCode;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.Positive;
import lombok.RequiredArgsConstructor;

/**
 * 채팅방 메시지 처리 API
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

	/**
	 * 메시지 전체 조회 API
	 *
	 * <p>
	 *  클라이언트로부터 채팅방 ID와 커서(lastId), 페이지 크기(size)를 전달받아
	 *  커서 기반 페이징 방식으로 메시지 목록을 조회하고, 표준 응답 형식({@link ApiResponse})으로 반환합니다.
	 * </p>
	 */
	@GetMapping("/room/{roomId}/messages")
	public ApiResponse<GetMessageListRes> getMessages(
		@PathVariable @Positive Long roomId,
		@RequestParam(required = false) @Positive Long lastId,
		@RequestParam(defaultValue = "20") @Min(1) @Max(50) int size
	) {
		Long userId = 1L;
		GetMessageListRes response = messageService.findMessagePageByCursor(userId, roomId, lastId, size);
		return ApiResponse.of(SuccessCode.OK, response);
	}

	/**
	 * 메시지 읽음 처리 API
	 */
	@PostMapping("/room/{roomId}/read")
	public ApiResponse<Void> readMessages(
		@PathVariable @Positive Long roomId
	) {
		Long userId = 1L;
		messageService.readAllMessages(userId, roomId);
		return ApiResponse.ok();
	}

}
