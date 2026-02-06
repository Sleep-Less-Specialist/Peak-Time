package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;

/**
 * 비밀번호 재설정 요청 DTO
 * <p>
 * 사용자가 비밀번호 재설정을 요청할 때 이메일 주소를 전달하기 위해 사용한다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 5.
 */
public record PasswordResetReq(

	@Email(message = "올바른 이메일 형식이 아닙니다.")
	@NotBlank(message = "이메일은 필수입니다.")
	String email
) {}