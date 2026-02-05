package com.github.sleeplessspecialist.peaktime.domain.auth.service;

import java.time.Duration;
import java.util.UUID;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.auth.exception.AuthErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.auth.token.PasswordResetTokenStore;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.exception.UserErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.mail.MailService;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 비밀번호 초기화(Password Reset) 요청 및 확정 처리를 담당하는 서비스 클래스입니다.
 *
 * <p>
 * 비밀번호 초기화는 다음 두 단계로 구성됩니다.
 * <ul>
 *   <li>요청 단계: 이메일을 기반으로 초기화 토큰을 생성하고 메일로 전달</li>
 *   <li>확정 단계: 전달된 토큰을 검증한 뒤 새로운 비밀번호로 변경</li>
 * </ul>
 * </p>
 *
 * <p>
 * 비밀번호 초기화 토큰은 {@link PasswordResetTokenStore}를 통해 TTL 기반으로 관리되며, 확정 성공 시 1회성으로 제거됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 4.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class PasswordResetService {

	private static final Duration RESET_TOKEN_TTL = Duration.ofMinutes(15);
	@Value("${app.frontend.base-url}")
	private String frontendBaseUrl;

	private final UserRepository userRepository;
	private final PasswordResetTokenStore tokenStore;
	private final MailService mailService;
	private final PasswordEncoder passwordEncoder;

	/**
	 * 비밀번호 초기화 요청을 처리합니다.
	 *
	 * <p>
	 * 전달받은 이메일에 해당하는 사용자가 존재할 경우 비밀번호 재설정을 위한 토큰을 생성하고 메일로 전송합니다.
	 * 사용자가 존재하지 않더라도 보안 및 명세 일관성을 위해 동일한 성공 흐름으로 처리합니다.
	 * </p>
	 *
	 * @param email 비밀번호 초기화를 요청한 사용자 이메일
	 */
	public void requestReset(String email) {
		userRepository.findByEmail(email).ifPresentOrElse(user -> {
			String token = UUID.randomUUID().toString();
			tokenStore.save(token, user.getId(), RESET_TOKEN_TTL);

			String resetLink = buildResetLink(token);

			String html = """
				<p>비밀번호 재설정 요청이 접수되었습니다.</p>
				<p>아래 링크를 클릭하여 비밀번호를 재설정해주세요.</p>
				<p><a href="%s">비밀번호 재설정</a></p>
				<p>본 링크는 15분간 유효합니다.</p>
				<p>요청하지 않았다면 본 메일을 무시해주세요.</p>
				""".formatted(resetLink);

			mailService.sendHtml(
				user.getEmail(),
				"[Peak-Time] 비밀번호 재설정 안내",
				html
			);
		}, () -> {
			// 보안 및 명세 일관성을 위해 존재하지 않는 이메일에 대해서도 동일하게 처리(200 OK)
			log.info("비밀번호 재설정 요청 - 존재하지 않는 이메일(email={})", email);
		});
	}

	/**
	 * 비밀번호 초기화 확정을 처리합니다.
	 *
	 * <p>
	 * 전달받은 토큰을 검증한 뒤, 새로운 비밀번호로 변경하고 토큰을 1회성으로 제거합니다.
	 * </p>
	 *
	 * @param token 비밀번호 초기화 토큰
	 * @param newPassword 새 비밀번호
	 */
	public void confirmReset(String token, String newPassword) {
		Long userId = tokenStore.findUserId(token)
			.orElseThrow(() -> new CustomException(AuthErrorCode.PASSWORD_RESET_TOKEN_EXPIRED));

		User user = userRepository.findById(userId)
			.orElseThrow(() -> new CustomException(UserErrorCode.USER_NOT_FOUND));

		user.changePassword(passwordEncoder.encode(newPassword));
		userRepository.save(user);

		tokenStore.delete(token);
	}

	private String buildResetLink(String token) {
		return frontendBaseUrl + "/reset-password?token=" + token;
	}
}