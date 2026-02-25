package com.github.sleeplessspecialist.peaktime.global.common.aop;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.slf4j.MDC;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;

import jakarta.servlet.http.HttpServletRequest;

import java.lang.reflect.Method;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * 요청 단위 공통 로그를 남기기 위한 AOP 입니다.
 * <p>
 * 목적 : ELK/Kibana에서 검색/필터링이 가능하도록
 * method, endpoint(URI), params, userId, traceId, latencyMs 등의 필드를 일관되게 로그로 남깁니다.
 * </p>
 *
 * @author 재원
 * @version 1.1
 * @since 2026. 2. 25.
 */
@Aspect
@Component
public class RequestLoggingAspect {

	private static final Logger log = LoggerFactory.getLogger(RequestLoggingAspect.class);
	private static final String UNKNOWN = "UNKNOWN";

	private static final Set<String> SENSITIVE_KEYWORDS = Set.of(
		"password", "passwd", "pwd",
		"token", "accesstoken", "refreshtoken",
		"auth", "authorization",
		"email", "phone", "phonenumber", "mobile"
	);

	/**
	 * {@org.springframework.web.bind.annotation.RestController} 요청을 감싸서 공통 요청 로그를 남깁니다.
	 * <p>
	 * 기록 필드:	method / endpoint / params / userId / traceId / latencyMs
	 * </p>
	 */
	@Around("within(@org.springframework.web.bind.annotation.RestController *)")
	public Object logRequest(ProceedingJoinPoint joinPoint) throws Throwable {
		long startNs = System.nanoTime();

		HttpServletRequest request = currentRequest();
		String method = resolveMethod(request);
		String endpoint = resolveEndpoint(request);
		String params = resolveParams(request);
		Long userId = resolveUserId();
		String traceId = resolveTraceId();

		try {
			Object result = joinPoint.proceed();
			long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
			logRequestSuccess(method, endpoint, userId, traceId, latencyMs, params);
			return result;
		} catch (Error err) {
			throw err;
		} catch (Throwable ex) {
			long latencyMs = TimeUnit.NANOSECONDS.toMillis(System.nanoTime() - startNs);
			logRequestFailure(method, endpoint, userId, traceId, latencyMs, params, ex);
			throw ex;
		}
	}

	private void logRequestSuccess(String method, String endpoint, Long userId, String traceId,
		long latencyMs, String params) {
		if (hasText(params)) {
			log.info("request method={} endpoint={} userId={} traceId={} latencyMs={} params={}",
				method, endpoint, userId, traceId, latencyMs, params);
			return;
		}
		log.info("request method={} endpoint={} userId={} traceId={} latencyMs={}",
			method, endpoint, userId, traceId, latencyMs);
	}

	private void logRequestFailure(String method, String endpoint, Long userId, String traceId,
		long latencyMs, String params, Throwable ex) {
		String exName = ex.getClass().getSimpleName();

		if (hasText(params)) {
			log.warn("request_failed method={} endpoint={} userId={} traceId={} latencyMs={} params={} ex={}",
				method, endpoint, userId, traceId, latencyMs, params, exName);
			return;
		}
		log.warn("request_failed method={} endpoint={} userId={} traceId={} latencyMs={} ex={}",
			method, endpoint, userId, traceId, latencyMs, exName);
	}

	private HttpServletRequest currentRequest() {
		try {
			ServletRequestAttributes attrs = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
			if (attrs == null) {
				return null;
			}
			return attrs.getRequest();
		} catch (Exception ignored) {
			return null;
		}
	}

	private String resolveMethod(HttpServletRequest request) {
		if (request == null) {
			return UNKNOWN;
		}
		String method = request.getMethod();
		if (!hasText(method)) {
			return UNKNOWN;
		}
		return method;
	}

	private String resolveEndpoint(HttpServletRequest request) {
		if (request == null) {
			return UNKNOWN;
		}
		String uri = request.getRequestURI();
		if (!hasText(uri)) {
			return UNKNOWN;
		}
		return uri;
	}

	private String resolveTraceId() {
		String traceId = MDC.get("traceId");
		if (hasText(traceId)) {
			return traceId;
		}
		String requestId = MDC.get("requestId");
		if (hasText(requestId)) {
			return requestId;
		}
		return null;
	}

	private String resolveParams(HttpServletRequest request) {
		if (request == null) {
			return null;
		}

		Map<String, String[]> paramMap = request.getParameterMap();
		if (paramMap == null || paramMap.isEmpty()) {
			return null;
		}

		StringBuilder sb = new StringBuilder();
		int totalMax = 500; // 전체 params 문자열 최대 길이

		for (Map.Entry<String, String[]> entry : paramMap.entrySet()) {
			String key = entry.getKey();
			if (!hasText(key)) {
				continue;
			}
			String sanitizedKey = sanitize(key);
			if (!hasText(sanitizedKey)) {
				continue;
			}
			String lower = sanitizedKey.toLowerCase();
			boolean sensitive = containsAny(lower, SENSITIVE_KEYWORDS);

			String value = sensitive ? "***" : sanitize(joinAndTruncate(entry.getValue(), 100));
			if (sb.length() > 0) {
				sb.append("&");
			}
			sb.append(sanitizedKey).append("=").append(value);

			if (sb.length() >= totalMax) {
				sb.setLength(totalMax);
				sb.append("...");
				break;
			}
		}

		return sb.length() == 0 ? null : sb.toString();
	}

	private String joinAndTruncate(String[] values, int maxLen) {
		if (values == null || values.length == 0) {
			return "";
		}
		String joined = String.join(",", values);
		if (!hasText(joined)) {
			return "";
		}
		if (joined.length() <= maxLen) {
			return joined;
		}
		return joined.substring(0, maxLen) + "...";
	}

	private Long resolveUserId() {
		Authentication auth = SecurityContextHolder.getContext().getAuthentication();
		if (auth == null) {
			return null;
		}
		Object principal = auth.getPrincipal();
		if (principal == null) {
			return null;
		}

		Long id = tryResolveIdByReflection(principal, "getId");
		if (id != null) {
			return id;
		}
		id = tryResolveIdByReflection(principal, "getUserId");
		if (id != null) {
			return id;
		}

		if (principal instanceof UserDetails) {
			return null;
		}

		// 3) principal이 문자열로 들어오는 경우(예: anonymousUser)
		if (principal instanceof String) {
			String value = (String)principal;
			if (!hasText(value) || "anonymousUser".equalsIgnoreCase(value)) {
				return null;
			}
		}

		return null;
	}

	private Long tryResolveIdByReflection(Object principal, String methodName) {
		try {
			Method m = principal.getClass().getMethod(methodName);
			Object value = m.invoke(principal);
			if (value instanceof Long) {
				return (Long)value;
			}
			if (value instanceof Integer) {
				return ((Integer)value).longValue();
			}
			if (value instanceof String) {
				String s = (String)value;
				if (hasText(s)) {
					return Long.parseLong(s);
				}
			}
			return null;
		} catch (Exception ignored) {
			return null;
		}
	}

	private boolean containsAny(String lowerKey, Set<String> keywords) {
		if (!hasText(lowerKey) || keywords == null || keywords.isEmpty()) {
			return false;
		}
		for (String k : keywords) {
			if (hasText(k) && lowerKey.contains(k)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * 로그 파싱/검색을 깨뜨릴 수 있는 제어문자(개행/탭 등)를 제거합니다.
	 */
	private String sanitize(String value) {
		if (!hasText(value)) {
			return value;
		}
		String sanitized = value.replace('\n', ' ').replace('\r', ' ').replace('\t', ' ');
		StringBuilder sb = new StringBuilder(sanitized.length());
		for (int i = 0; i < sanitized.length(); i++) {
			char c = sanitized.charAt(i);
			if (c < 0x20) {
				sb.append(' ');
				continue;
			}
			sb.append(c);
		}
		return sb.toString().trim();
	}

	private boolean hasText(String value) {
		return value != null && !value.trim().isEmpty();
	}

}