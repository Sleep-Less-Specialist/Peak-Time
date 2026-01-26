package com.github.sleeplessspecialist.peaktime.domain.auth.dto.response;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * SignupRes 클래스입니다.
 * <p>
 * 회원가입 요청 이후 응답 처리를 반환하는 DTO입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@AllArgsConstructor
public class SignupRes {

	private Long userId;
	private String email;
	private String name;

}