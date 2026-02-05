package com.github.sleeplessspecialist.peaktime.domain.user.dto;

import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 내 정보 수정 요청 시 클라이언트로부터 전달받는 데이터를 담는 DTO입니다.
 * 변경 가능한 정보인 사용자 이름과 연락처 정보를 포함합니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */

@Getter
@NoArgsConstructor
public class UserUpdateReq {
	private String name;
	private String phoneNumber;
}
