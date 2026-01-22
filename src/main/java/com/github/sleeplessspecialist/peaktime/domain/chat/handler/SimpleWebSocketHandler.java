package com.github.sleeplessspecialist.peaktime.domain.chat.handler;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;
import org.springframework.web.socket.handler.TextWebSocketHandler;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;


/**
 * /connect 로 웹소켓 연결 요청이 들어왔을때 이를 처리할 클래스
 * <p>
 * 클라이언트가 서버로 커넥션을 맻으면, 서버는 session을 만들어서 커넥션을 맺은 클라이언트들을 등록한다.
 * 그리고 메시지를 전달받으면, session 에 저장되어있는 모든 클라이언트에게 메시지를 발송한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */

@Slf4j
@Component
public class SimpleWebSocketHandler extends TextWebSocketHandler {

    /**
     * thread-safe 한 ConcurrentHashMap.newKeySet() 사용
     */
    private final Set<WebSocketSession> sessions =ConcurrentHashMap.newKeySet();

    /**
     * 연결 된 직후, 서버에서 웹소켓이 관리하는 세션에 저장
     */
    @Override
    public void afterConnectionEstablished(WebSocketSession session) throws Exception {

        sessions.add(session);
        log.info("Connected to {}", session.getId());
    }

    /**
     * 메시지를 클라이언트에게 전달 받을 경우, Connect 되어 있는 사용자에게 메시지를 수신하는 역할
     * sessions 에 저장된 연결된 클라이언트들이 열려있다면 메시지를 수신한다.
     */
    protected void handleTextMessage(WebSocketSession session, TextMessage message) throws Exception {

        String payload = message.getPayload();
        log.info("Received message content is {}", payload);
        for(WebSocketSession s : sessions){
            if(s.isOpen()){
                s.sendMessage(new TextMessage(payload));
            }
        }
    }

    /**
     * 연결이 끊기면, session 에서 삭제 하기
     */
    @Override
    public void afterConnectionClosed(WebSocketSession session, CloseStatus status) throws Exception {

        sessions.remove(session);
        log.info("Disconnected from {}", session.getId());
    }

}
