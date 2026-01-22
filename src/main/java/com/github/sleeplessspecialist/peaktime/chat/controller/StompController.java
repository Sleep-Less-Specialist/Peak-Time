package com.github.sleeplessspecialist.peaktime.chat.controller;

import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.DestinationVariable;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

/**
 * STOMP 메시지 기반 채팅 요청을 처리하는 컨트롤러
 *
 * <p>
 * WebSocket + STOMP 구조에서 클라이언트가 발행한 메시지를
 *  @MessageMapping 기반으로 라우팅하여 처리한다.
 * </p>
 *
 *  <p>
 *  순수 WebSocket 방식에서 필요했던 세션 관리 및 메시지 라우팅 로직을 제거하고,
 *  STOMP가 제공하는 pub/sub 및 어노테이션 기반 메시지 처리 구조를 활용한다.
 *  </p>
 *
 *  <p>
 *  /publish/{roomId}  @DestinationVariable 를 사용하여 경로변수 roomId 받아오기, 메시지 받아오기
 *  /topic/{roomId} 으로 메시지를 발행하여 해당 roomId 를 구독중인 클라이언트 에게 메시지 전송
 *  </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Slf4j
@Controller
class StompController {

    /**
     * stomp 메시지 브로커 의 내부 동작 선언 메서드
     */
    @MessageMapping("/{roomId}")
    @SendTo("/topic/{roomId}")
    public String sendMessage(@DestinationVariable Long roomId, String message){

        log.info("roomId: {}, message: {}", roomId, message);
        return message;
    }
}
