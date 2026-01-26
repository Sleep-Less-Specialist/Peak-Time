package com.github.sleeplessspecialist.peaktime.domain.auth;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.data.jpa.JpaRepositoriesAutoConfiguration;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.autoconfigure.orm.jpa.HibernateJpaAutoConfiguration;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.domain.auth.controller.AuthController;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.request.SignupReq;
import com.github.sleeplessspecialist.peaktime.domain.auth.dto.response.SignupRes;
import com.github.sleeplessspecialist.peaktime.domain.auth.service.AuthService;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenProvider;

/**
 * {@link AuthController}의 HTTP 요청/응답 바인딩을 검증하는 컨트롤러 단위 테스트입니다.
 * <p>
 * 서비스/DB 로직은 {@link org.springframework.test.context.bean.override.mockito.MockitoBean}
 * 으로 대체하고,
 * 컨트롤러가 요청을 정상적으로 수신하여 서비스에 위임하는지와
 * 기본 응답 코드(HTTP 200/400 등)를 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 25.
 */
@ActiveProfiles("test")
@WebMvcTest(controllers = AuthController.class)
public class AuthControllerTest {

	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockitoBean
	private AuthService authService;

	@MockitoBean
	private JwtTokenProvider jwtTokenProvider;


	/**
	 * 회원가입 요청이 정상 입력값으로 전달되었을 때,
	 * 컨트롤러가 HTTP 200(OK)을 반환하고 서비스의 {@code signup} 메서드에 위임하는지 검증합니다.
	 *
	 * @throws Exception MockMvc 수행 중 예외가 발생할 수 있습니다.
	 */
	@Test
	@DisplayName("회원가입 요청 성공 시 응답 검증")
	void signup_success_return_ok() throws Exception {
		// given
		SignupReq request = new SignupReq(
			"test@email.com",
			"Aa!12345678",
			"재원",
			"010-1234-5678"
		);

		SignupRes mockResponse = new SignupRes(1L, "test@email.com", "재원");
		when(authService.signup(any(SignupReq.class))).thenReturn(mockResponse);

		// when & then
		mockMvc.perform(post("/api/v1/auth/signup")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(request)))
			.andExpect(status().isOk());

		verify(authService, times(1)).signup(any(SignupReq.class));
	}
}