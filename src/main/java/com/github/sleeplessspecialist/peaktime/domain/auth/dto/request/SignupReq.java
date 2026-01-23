package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * SignupReq 클래스입니다.
 * <p>
 * 회원가입 요청 시 내용을 담아내는 DTO입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 23.
 */
@Getter
@NoArgsConstructor
@AllArgsConstructor
public class SignupReq {

	@Email
	@NotBlank
	private String email;

	@NotBlank
	@Size(min = 8, max = 64)
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*[!@#$%^&*()_+\\-=[\\]{};':\"\\\\|,.<>/?]).+$",
		message = "비밀번호는 대문자, 소문자, 특수문자를 각각 1개 이상 포함해야 합니다."
	)
	private String password;

	@NotBlank
	@Size(min = 1, max = 20)
	private String name;

	@NotBlank
	@Pattern(
		regexp = "^[0-9\\-]{10,13}$",
		message = "휴대폰 번호 형식이 올바르지 않습니다."
	)
	private String phoneNumber;

}