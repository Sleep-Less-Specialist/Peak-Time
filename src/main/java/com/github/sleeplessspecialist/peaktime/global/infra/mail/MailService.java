package com.github.sleeplessspecialist.peaktime.global.infra.mail;

import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.RequiredArgsConstructor;

/**
 * SMTP 기반 메일 발송을 담당하는 서비스 클래스입니다.
 *
 * <p>
 * 비밀번호 초기화, 이메일 인증 등 외부 SMTP 서버(Gmail)와 통신하는 작업은
 * 네트워크 지연 및 실패 가능성이 높아 요청 스레드를 블로킹하지 않도록 비동기로 처리합니다.
 * </p>
 *
 * <p>
 * 실제 전송은 {@link JavaMailSender}를 통해 수행되며,
 * 메일 발송 실패 시 공통 예외 규약에 맞춰 {@link CustomException}으로 래핑합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 3.
 */
@Service
@RequiredArgsConstructor
public class MailService {

	private final JavaMailSender mailSender;

	@Value("${spring.mail.username}")
	private String from;

	/**
	 * HTML 본문 메일을 비동기로 발송합니다.
	 *
	 * @param to 수신자 이메일
	 * @param subject 메일 제목
	 * @param htmlBody HTML 본문
	 */
	@Async("mailAsyncExecutor")
	public void sendHtml(String to, String subject, String htmlBody) {
		MimeMessage mimeMessage = mailSender.createMimeMessage();
		try {
			MimeMessageHelper helper = new MimeMessageHelper(
				mimeMessage,
				MimeMessageHelper.MULTIPART_MODE_MIXED_RELATED,
				StandardCharsets.UTF_8.name()
			);
			helper.setFrom(from);
			helper.setTo(to);
			helper.setSubject(subject);
			helper.setText(htmlBody, true);

			mailSender.send(mimeMessage);
		}
		catch (MailException | MessagingException e) {
			throw new CustomException(MailErrorCode.MAIL_SEND_FAILED, e);
		}
	}
}