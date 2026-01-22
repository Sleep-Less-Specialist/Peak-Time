package com.github.sleeplessspecialist.peaktime.global.common.security.handler;

import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorResponse;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

import java.io.IOException;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 인가 실패(403) 처리 Handler 입니다.
 * <p>
 * 인증은 되었으나 해당 리소스에 접근할 권한이 없는 경우 호출되며,
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
public class RestAccessDeniedHandler implements AccessDeniedHandler {

	private final ObjectMapper objectMapper;

	/**
	 * 인가(Authorization)에 실패했을 때 호출되는 메서드입니다.
	 *
	 * <p>
	 * 인증은 완료되었으나 사용자가 요청한 리소스에 접근할 권한이 없는 경우,
	 * Spring Security 필터 체인 단계에서 실행되며
	 * 컨트롤러에 진입하기 전에 HTTP 403(Forbidden) 상태 코드와 함께
	 * 공통 실패 응답 포맷({@link ErrorResponse})을 JSON 형태로 반환합니다.
	 * </p>
	 *
	 * @param request 인가 실패가 발생한 HTTP 요청 객체
	 * @param response 인가 실패 응답을 작성할 HTTP 응답 객체
	 * @param accessDeniedException 인가 실패 원인에 대한 예외 정보
	 * @throws IOException 응답 본문(JSON) 작성 중 발생할 수 있는 예외
	 * @throws ServletException 서블릿 처리 과정에서 발생할 수 있는 예외
	 */
	@Override
	public void handle(
		HttpServletRequest request,
		HttpServletResponse response,
		AccessDeniedException accessDeniedException
	) throws IOException, ServletException {

		log.warn("인가 실패. requestURI={}, method={}, exception={}",
			request.getRequestURI(),
			request.getMethod(),
			accessDeniedException.getClass().getSimpleName()
		);

		response.setStatus(HttpServletResponse.SC_FORBIDDEN);
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		response.setCharacterEncoding("UTF-8");

		GlobalErrorCode errorCode = GlobalErrorCode.ACCESS_DENIED;
		ErrorResponse body = ErrorResponse.from(errorCode);

		objectMapper.writeValue(response.getWriter(), body);
	}

}