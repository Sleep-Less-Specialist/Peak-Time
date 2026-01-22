package com.github.sleeplessspecialist.peaktime.global.common.security.handler;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorResponse;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.stereotype.Component;


/**
 * 인증 실패(401) 처리 EntryPoint 입니다.
 * <p>
 * 보호된 리소스에 대해 인증 정보가 없거나 유효하지 않을 때 호출되며,
 * 서비스의 공통 실패 응답 포맷(ErrorResponse)으로 JSON 에러를 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Slf4j
@RequiredArgsConstructor
@Component
public class RestAuthenticationEntryPoint implements AuthenticationEntryPoint {

	private final ObjectMapper objectMapper;

	/**
	 * 인증되지 않은 사용자가 보호된 리소스에 접근할 때 호출됩니다.
	 * <p>
	 * Spring Security FilterChain에서 인증 실패 발생 시
	 * 컨트롤러 진입 전에 본 메서드가 실행되며, HTTP 401 상태 코드와 함께
	 * 서비스 공통 실패 응답 포맷을 JSON 형태로 반환합니다.
	 * </p>
	 *
	 * @param request       인증 실패가 발생한 HTTP 요청
	 * @param response      인증 실패 응답을 작성할 HTTP 응답
	 * @param authException 인증 실패 원인에 대한 예외 정보
	 * @throws IOException      응답 본문 작성 중 발생할 수 있는 예외
	 * @throws ServletException 서블릿 처리 중 발생할 수 있는 예외
	 */
	@Override
	public void commence(
		HttpServletRequest request,
		HttpServletResponse response,
		AuthenticationException authException
	) throws IOException, ServletException {

		log.warn(
			"인증 실패. requestURI={}, method={}, exception={}",
			request.getRequestURI(),
			request.getMethod(),
			authException.getClass().getSimpleName()
		);

		response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		GlobalErrorCode errorCode = GlobalErrorCode.UNAUTHORIZED;

		ErrorResponse body = ErrorResponse.from(errorCode);

		objectMapper.writeValue(response.getWriter(), body);
	}
}