package com.github.sleeplessspecialist.peaktime.global.common.security.filter;

import java.io.IOException;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

/**
 * TraceIdAccessLogFilter 클래스입니다.
 * <p>
 * 요청 시작 시점에 traceId를 생성하여 MDC에 주입하고, 요청 처리가 완료된 이후(응답 커밋 이후)
 * 최종 HTTP 상태코드(status)와 처리시간(latencyMs)을 access 로그로 남깁니다.
 * </p>
 *
 * <p>
 * 목적:
 * <ul>
 *   <li>ELK/Kibana에서 traceId 기반 요청 단위 추적</li>
 *   <li>AOP/Service 로그와의 상관관계(동일 traceId) 확보</li>
 *   <li>예외 처리(@ControllerAdvice) 이후 확정된 최종 status 로깅</li>
 * </ul>
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 2. 25.
 */
@Component
@Order(Ordered.HIGHEST_PRECEDENCE)
public class TraceIdAccessLogFilter extends OncePerRequestFilter {

	private static final Logger log = LoggerFactory.getLogger(TraceIdAccessLogFilter.class);

	/**
	 * 요청의 전체 lifecycle(시작 ~ 응답 완료)을 감싸는 핵심 로직입니다.
	 * <p>
	 * 동작 순서:
	 * <ol>
	 *   <li>요청 시작 시 traceId를 생성하여 MDC에 주입</li>
	 *   <li>filterChain을 통해 실제 Controller 및 이후 필터 로직 실행</li>
	 *   <li>요청 처리 완료 후 최종 HTTP status와 latency를 계산</li>
	 *   <li>access 로그를 한 줄로 기록</li>
	 *   <li>MDC에서 traceId 제거</li>
	 * </ol>
	 * </p>
	 *
	 * @param request  현재 HTTP 요청 객체
	 * @param response 현재 HTTP 응답 객체
	 * @param filterChain 다음 필터 체인
	 * @throws ServletException 서블릿 처리 중 예외 발생 시
	 * @throws IOException 입출력 처리 중 예외 발생 시
	 */
	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
		throws ServletException, IOException {
		long startNs = System.nanoTime();

		String traceId = MDC.get("traceId");
		if (traceId == null || traceId.isBlank()) {
			traceId = UUID.randomUUID().toString();
			MDC.put("traceId", traceId);
		}

		try {
			filterChain.doFilter(request, response);
		} finally {
			long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);

			String method = request.getMethod();
			String endpoint = request.getRequestURI();
			int status = response.getStatus();
			Long userId = resolveUserId();

			// 최종 응답 기준 access log (정확한 status)
			log.info("access method={} endpoint={} status={} userId={} traceId={} latencyMs={}",
				method, endpoint, status, userId, traceId, latencyMs);

			// MDC 정리
			MDC.remove("traceId");
		}
	}

	private Long resolveUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null || auth.getPrincipal() == null) {
			return null;
		}
		Object principal = auth.getPrincipal();

		// 프로젝트에서 principal을 userId(Long)로 넣는 경우를 우선 처리
		if (principal instanceof Long)
			return (Long)principal;
		if (principal instanceof Integer)
			return ((Integer)principal).longValue();

		return null;
	}
}
