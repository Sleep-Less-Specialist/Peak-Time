package com.github.sleeplessspecialist.peaktime.global.common.security.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.servlet.config.annotation.ViewControllerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import org.springframework.boot.web.servlet.FilterRegistrationBean;

import com.github.sleeplessspecialist.peaktime.global.common.security.filter.JwtAuthenticationFilter;
import com.github.sleeplessspecialist.peaktime.global.common.security.filter.TraceIdAccessLogFilter;
import com.github.sleeplessspecialist.peaktime.global.common.security.handler.OAuth2SuccessHandler;
import com.github.sleeplessspecialist.peaktime.global.common.security.handler.RestAccessDeniedHandler;
import com.github.sleeplessspecialist.peaktime.global.common.security.handler.RestAuthenticationEntryPoint;
import com.github.sleeplessspecialist.peaktime.global.common.security.oauth2.CustomOAuth2UserService;
import com.github.sleeplessspecialist.peaktime.global.common.security.policy.SecurityPathPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Spring Security 설정에서 사용하는 경로 정책을 정의합니다.
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
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final CustomOAuth2UserService customOAuth2UserService;
	private final OAuth2SuccessHandler oAuth2SuccessHandler;

	/**
	 * Spring Security 필터 체인을 정의합니다.
	 *
	 * <p>
	 * 인증 관련 엔드포인트(/api/v1/auth/**)는 허용하며, 그 외 요청은 JWT 인증을 요구하도록 구성합니다.
	 * </p>
	 *
	 * @param http HttpSecurity 설정 객체
	 * @return 구성된 {@link SecurityFilterChain}
	 * @throws Exception 보안 설정 과정에서 발생할 수 있는 예외
	 */
	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http,
		TraceIdAccessLogFilter traceIdAccessLogFilter) throws Exception {
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

			// OAuth2 Login configuration (Kakao)
			// OAuth2 인증 흐름은 서버에서 처리하고, 로그인 성공 후 JWT를 발급한다.
			.oauth2Login(oauth2 -> oauth2
				.redirectionEndpoint(redirection -> redirection
					.baseUri("/api/v2/oauth2/code/*")
				)
				// 사용자 정보 매핑
				.userInfoEndpoint(userInfo -> userInfo
					.userService(customOAuth2UserService)
				)
				// 로그인 성공 시 JWT 발급 처리
				.successHandler(oAuth2SuccessHandler)
			)

			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.addFilterAfter(traceIdAccessLogFilter, JwtAuthenticationFilter.class)

			.authorizeHttpRequests(auth -> auth
				// 명세 기반 공개 엔드포인트 + 공개 조회(GET)는 permitAll
				.requestMatchers(SecurityPathPolicy.PUBLIC_ENDPOINTS).permitAll()
				.requestMatchers(HttpMethod.GET, SecurityPathPolicy.PUBLIC_GET_ENDPOINTS).permitAll()
				.requestMatchers(SecurityPathPolicy.ADMIN_ENDPOINTS).hasRole("ADMIN")
				.requestMatchers(SecurityPathPolicy.LECTURER_ENDPOINTS).hasRole("LECTURER")
				// 그 외는 인증 필요
				.anyRequest().authenticated()
			);
		return http.build();
	}

	/**
	 * 비밀번호 해싱에 사용할 {@link PasswordEncoder} 빈을 등록합니다.
	 *
	 * <p>
	 * 회원가입/로그인 기능에서 비밀번호 암호화를 위해 사용하며,
	 * 현재는 {@link BCryptPasswordEncoder}를 기본 구현체로 사용합니다.
	 * </p>
	 *
	 * @return BCrypt 기반 {@link PasswordEncoder}
	 */
	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}

	/**
	 * 비밀번호 재설정 메일 링크(`/reset-password?token=...`)로 진입하면
	 * 서버가 JSON(401) 대신 프론트 엔트리 페이지(index.html)를 내려주도록 포워딩합니다.
	 *
	 * <p>
	 * index.html 내부 스크립트가 `token` 쿼리 파라미터를 읽어 '새 비밀번호 설정' 폼(섹션)을 표시합니다.
	 * </p>
	 */
	@Bean
	public WebMvcConfigurer passwordResetViewForwarder() {
		return new WebMvcConfigurer() {
			@Override
			public void addViewControllers(ViewControllerRegistry registry) {
				registry.addViewController("/reset-password").setViewName("forward:/index.html");
			}
		};
	}

	@Bean
	public FilterRegistrationBean<TraceIdAccessLogFilter> traceIdAccessLogFilterRegistration(TraceIdAccessLogFilter filter) {
		FilterRegistrationBean<TraceIdAccessLogFilter> registration = new FilterRegistrationBean<>(filter);
		registration.setEnabled(false);
		return registration;
	}

	@Bean
	public TraceIdAccessLogFilter traceIdAccessLogFilter() {
		return new TraceIdAccessLogFilter();
	}
}
