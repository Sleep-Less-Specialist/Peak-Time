package com.github.sleeplessspecialist.peaktime.global.common.security.filter;

import java.io.IOException;
import java.util.List;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorResponse;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenException;
import com.github.sleeplessspecialist.peaktime.global.common.security.jwt.JwtTokenProvider;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

/**
 * 요청의 Authorization 헤더에서 JWT를 추출하여 검증하고,
 * 유효한 경우 SecurityContext에 인증 정보를 설정하는 필터입니다.
 *
 * <p>
 * 토큰이 없으면 인증을 설정하지 않고 다음 필터로 전달합니다.
 * 토큰이 존재하지만 유효하지 않으면 401을 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

	private static final String BEARER_PREFIX = "Bearer ";

	private final JwtTokenProvider jwtTokenProvider;
	private final ObjectMapper objectMapper;

	@Override
	protected void doFilterInternal(
		HttpServletRequest request,
		HttpServletResponse response,
		FilterChain filterChain
	) throws ServletException, IOException {

		String token = extractToken(request);
		if (token == null) {
			filterChain.doFilter(request, response);
			return;
		}

		try {
			authenticate(request, token);
			filterChain.doFilter(request, response);
		} catch (JwtTokenException e) {
			handleUnauthorized(response, e);
			return;
		}
	}

	/**
	 * Authorization 헤더에서 Bearer 토큰을 추출합니다.
	 */
	private String extractToken(HttpServletRequest request) {
		String authorization = request.getHeader(HttpHeaders.AUTHORIZATION);
		if (authorization == null || !authorization.startsWith(BEARER_PREFIX)) {
			return null;
		}
		return authorization.substring(BEARER_PREFIX.length()).trim();
	}

	/**
	 * 토큰을 검증하고 SecurityContext에 인증 정보를 설정합니다.
	 */
	private void authenticate(HttpServletRequest request, String token) {
		jwtTokenProvider.validateToken(token);

		Long userId = jwtTokenProvider.getUserId(token);

		List<GrantedAuthority> authorities = extractAuthorities(token);

		UsernamePasswordAuthenticationToken authentication =
			new UsernamePasswordAuthenticationToken(userId, null, authorities);
		authentication.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));

		SecurityContextHolder.getContext().setAuthentication(authentication);
	}

	/**
	 * 토큰의 role claim을 기반으로 권한 정보를 생성합니다.
	 */
	private List<GrantedAuthority> extractAuthorities(String token) {
		String role = jwtTokenProvider.parseClaims(token).get("role", String.class);
		if (role == null || role.isBlank()) {
			return List.of();
		}
		return List.of(new SimpleGrantedAuthority(role));
	}

	/**
	 * 인증 실패 시 JWT 에러코드에 맞춰 공통 ErrorResponse를 반환합니다.
	 */
	private void handleUnauthorized(
		HttpServletResponse response,
		JwtTokenException exception
	) throws IOException {
		SecurityContextHolder.clearContext();

		JwtTokenErrorCode errorCode = exception.getErrorCode();
		ErrorResponse errorResponse = ErrorResponse.from(errorCode);

		response.setStatus(errorCode.getHttpStatus().value());
		response.setContentType(MediaType.APPLICATION_JSON_VALUE);
		objectMapper.writeValue(response.getWriter(), errorResponse);

	}
}