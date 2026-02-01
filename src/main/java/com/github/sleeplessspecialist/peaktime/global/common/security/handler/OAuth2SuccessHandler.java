package com.github.sleeplessspecialist.peaktime.global.common.security.handler;

import java.io.IOException;

import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * OAuth2 로그인 인증이 성공한 이후의 후처리를 담당하는 핸들러입니다.
 * <p>
 * Spring Security OAuth2 로그인 플로우에서 외부 인증 제공자(provider)를 통한
 * 인증이 성공하면 호출되며, 인증 성공 이후의 애플리케이션 동작을 정의합니다.
 * </p>
 *
 * <p>
 * 이후 단계에서 AccessToken / RefreshToken(JWT) 발급,
 * RefreshToken Redis 화이트리스트 저장,
 * 로그인 성공 후 리다이렉트 또는 응답 전략이 이 클래스에 추가될 예정입니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 30.
 */
@Component
@RequiredArgsConstructor
public class OAuth2SuccessHandler
	implements AuthenticationSuccessHandler {

	/**
	 * OAuth2 인증 제공자를 통한 로그인 인증이 성공했을 때 호출됩니다.
	 *
	 * <p>
	 * 이후 단계에서 사용자 유형(기존/신규)에 따른 분기 처리, JWT 발급 및 전달 전략(JSON 응답 또는 Redirect)이 이 메서드에 추가될 예정입니다.
	 * </p>
	 *
	 * @param request OAuth2 로그인 성공 요청 정보
	 * @param response OAuth2 로그인 성공 응답 객체
	 * @param authentication OAuth2 인증 완료 후 생성된 인증 정보
	 * @throws IOException 응답 처리 중 오류 발생 시
	 */
	@Override
	public void onAuthenticationSuccess(
		HttpServletRequest request,
		HttpServletResponse response,
		Authentication authentication) throws IOException {

		// 지금은 동작 확인용
		// 👉 다음 단계에서 JWT 발급 로직 추가
		response.setStatus(HttpServletResponse.SC_OK);
		response.setContentType("application/json;charset=UTF-8");

		response.getWriter().write("""
			{
			  "message": "OAuth2 login success"
			}
		""");
	}
}