package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 로그인 요청 DTO 클래스입니다.
 * <p>
 * 이메일과 비밀번호를 입력받아 로그인 요청을 보냅니다.
 * 공백일 수 없으며, 각 제약사항 위반 시 공통 에러를 반환합니다.
 * (정책) 로그인 시도 제한 등 보안 정책 위반은 공통 에러로 처리합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class LoginReq {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Size(min = 8, max = 64)
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
		message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
	)
	private String password;

}