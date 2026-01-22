package com.github.sleeplessspecialist.peaktime.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 *
 * 프론트로 보내는 메시지 json 데이터
 * <p>
 * message, 메시지를 보낸 사용자 name
 * 차후 필드 확장 가능
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class ChatMessageReqDto {

    private String message;
    private String name;
}
