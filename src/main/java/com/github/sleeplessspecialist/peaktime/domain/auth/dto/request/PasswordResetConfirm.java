package com.github.sleeplessspecialist.peaktime.domain.auth.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 비밀번호 재설정 확정 요청 DTO
 * <p>
 * 사용자가 이메일로 받은 재설정 링크(토큰)를 통해 새 비밀번호로 변경을 확정할 때 사용하는 요청 객체이다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 5.
 */
public record PasswordResetConfirm(

	@NotBlank(message = "토큰은 필수입니다.")
	String token,

	@NotBlank(message = "새 비밀번호는 필수입니다.")
	@Pattern(
		regexp = "^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[!@#$%^&*]).+$",
		message = "비밀번호는 대문자, 소문자, 숫자, 특수문자를 각각 1자 이상 포함해야 합니다."
	)
	String newPassword
) {
}