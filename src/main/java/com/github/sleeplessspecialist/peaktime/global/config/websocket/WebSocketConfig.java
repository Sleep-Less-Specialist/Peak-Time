package com.github.sleeplessspecialist.peaktime.global.config.websocket;

import com.github.sleeplessspecialist.peaktime.chat.handler.SimpleWebSocketHandler;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.socket.config.annotation.EnableWebSocket;
import org.springframework.web.socket.config.annotation.WebSocketConfigurer;
import org.springframework.web.socket.config.annotation.WebSocketHandlerRegistry;


/**
 * 웹소켓 설정을 추가할 설정 클래스
 *
 * <p>
 * 설정 클래스를 통해서 클라이언트가 서버와 /connect  를 맺고, 메시지를 발송하고 처리하는 핸들러를 등록한다.
 * /connect url 로 webSocket 연결 요청이 들어오면, 핸들러 클래스가 처리한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */

//@Configuration
@EnableWebSocket
@RequiredArgsConstructor
public class WebSocketConfig implements WebSocketConfigurer {

    private final SimpleWebSocketHandler simpleWebSocketHandler;

    /**
     * 웹소켓 핸들러 등록하기
     *
     */
    @Override
    public void registerWebSocketHandlers(WebSocketHandlerRegistry registry) {

        registry.addHandler(simpleWebSocketHandler,"/connect")
                .setAllowedOrigins("*"); // ws 프로토콜에서 cors 처리
    }
}
