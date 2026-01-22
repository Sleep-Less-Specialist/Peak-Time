package com.github.sleeplessspecialist.peaktime.global.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;

import com.github.sleeplessspecialist.peaktime.global.common.security.handler.RestAccessDeniedHandler;
import com.github.sleeplessspecialist.peaktime.global.common.security.handler.RestAuthenticationEntryPoint;
import com.github.sleeplessspecialist.peaktime.global.common.security.policy.SecurityPathPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security의 기본 필터 체인을 구성하는 설정 클래스입니다.
 *
 * <p>
 * 초기 개발 단계에서는 모든 요청을 허용하며,
 * 인증/인가 실패에 대한 공통 처리(401/403 JSON 응답)만 연결합니다.
 * </p>
 *
 * <p>
 * 이후 인증/인가 정책이 확정되면,
 * {@link SecurityPathPolicy}의 경로를 기준으로 점진적으로 보호 정책을 강화합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

	private final RestAuthenticationEntryPoint authenticationEntryPoint;
	private final RestAccessDeniedHandler accessDeniedHandler;

	/**
	 * Spring Security 필터 체인을 정의합니다.
	 *
	 * <p>
	 * 초기 개발 단계에서는 기능 개발을 막지 않기 위해 모든 요청을 허용합니다.
	 * 다만, 인증 인가 정책이 적용되는 시점에 대비하여 401,403 공통 처리 핸들러는 미리 연결합니다.
	 * </p>
	 *
	 * @param http HttpSecurity 설정 객체
	 * @return 구성된 {@link SecurityFilterChain}
	 * @throws Exception 보안 설정 과정에서 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {

		http
			.csrf(csrf -> csrf.disable())
			.sessionManagement(session
				-> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
			.formLogin(form -> form.disable())
			.httpBasic(basic -> basic.disable())

			.exceptionHandling(exception -> exception
				.authenticationEntryPoint(authenticationEntryPoint)
				.accessDeniedHandler(accessDeniedHandler)
			)

			.authorizeHttpRequests(auth -> auth
				.anyRequest().permitAll()
			);
		return http.build();
	}
}
