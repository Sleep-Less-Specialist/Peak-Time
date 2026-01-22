package com.github.sleeplessspecialist.peaktime.global.config.websocket;

import org.springframework.context.annotation.Configuration;
import org.springframework.messaging.simp.config.MessageBrokerRegistry;
import org.springframework.web.socket.config.annotation.EnableWebSocketMessageBroker;
import org.springframework.web.socket.config.annotation.StompEndpointRegistry;
import org.springframework.web.socket.config.annotation.WebSocketMessageBrokerConfigurer;

/**
 * STOMP 프로토콜 기반 WebSocket 메시징 설정 클래스
 * <p>
 * 메시지 브로커 를 통한 간편한 메시지 라우팅기능 적용
 * </p>
 *
 *  <p>
 *  /connect : Stomp 핸드 셰이크 진입점
 *  publish/{roomId} : 메시지 발행 조건
 *  /publish 가 접드어로 붙은 path 의 메시지가 발행이 되면 @Controller 객체의 @MessageMapping 메서드로 라우팅 된다.
 *  topic/{roomId}:  메시지 수신 조건
 *  </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Configuration
@EnableWebSocketMessageBroker // Broker 라는 문구가 붙으면 webSocket -> Stomp 로 마이그레이션
public class StompWebSocketConfig implements WebSocketMessageBrokerConfigurer {

    /**
     * STOMP 프로토콜 기반 통신을 위한 WebSocket 엔드포인트 설정
     */
    @Override
    public void registerStompEndpoints(StompEndpointRegistry registry) {

        registry.addEndpoint("/connect")
                .setAllowedOrigins("*")
                .withSockJS();
    }
    /**
     *  stomp 메시지 브로커 설정 메서드
     */
    @Override
    public void configureMessageBroker(MessageBrokerRegistry registry) {

        registry.setApplicationDestinationPrefixes("/publish");
        registry.enableSimpleBroker("/topic");
    }
}