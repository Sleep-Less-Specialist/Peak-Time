package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;

import java.util.Date;
import java.util.UUID;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.JwtBuilder;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import io.jsonwebtoken.security.SecurityException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 토큰을 생성·검증·파싱하는 Provider 클래스입니다.
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>Access / Refresh Token 생성</li>
 *   <li>토큰 서명·만료·형식 검증</li>
 *   <li>Claims 및 사용자 식별자 추출</li>
 * </ul>
 *
 * <h2>Refresh Token 설계</h2>
 * <p>
 * Refresh Token에는 세션 식별을 위한 <b>sid(session id)</b>와
 * 토큰 고유 식별자인 <b>jti(JWT ID)</b>가 포함됩니다.
 * 이는 Refresh Token Rotation(RTR) 구현을 위한 기반 구조로,
 * 세션 단위 관리 및 재사용 탐지(reuse detection)를 지원합니다.
 * </p>
 *
 * <p>
 * 본 클래스는 <b>토큰 자체</b>에 대한 책임만 가집니다.
 * 요청에서 토큰을 추출하고 SecurityContext에 인증 정보를 설정하는 역할은
 * 인증 필터(JwtAuthenticationFilter)에서 수행합니다.
 * </p>
 *
 * @author 재원
 * @version 1.1
 * @since 2026. 2. 12.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    /** 권한 정보를 담는 Claim 키 (Access Token 전용) */
    public static final String CLAIM_ROLE = "role";

    /** Refresh Token 세션 식별을 위한 Claim 키 (RTR 전용) */
    public static final String CLAIM_SID = "sid";

    private final JwtProperties jwtProperties;
    private final JwtKeyProvider jwtKeyProvider;

    /**
     * Access Token을 생성합니다.
     *
     * @param userId 사용자 ID
     * @param role   사용자 권한 (예: ROLE_ADMIN, ROLE_LECTURER)
     * @return Access Token
     */
    public String createAccessToken(Long userId, String role) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getAccessTokenExpirationMs());

        JwtBuilder builder = Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(expiry)
            .claim(CLAIM_ROLE, role)
            .signWith(jwtKeyProvider.getSigningKey());

        return builder.compact();
    }

    /**
     * Refresh Token을 생성합니다.
     *
     * <p>
     * Refresh Token에는 다음 정보가 포함됩니다:
     * <ul>
     *   <li>sub : 사용자 식별자 (userId)</li>
     *   <li>sid : 세션 식별자 (sessionId)</li>
     *   <li>jti : 토큰 고유 식별자 (재사용 탐지용)</li>
     * </ul>
     *
     * 이는 Refresh Token Rotation(RTR) 기반 세션 관리 및 재사용 탐지를 위한 구조입니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @param sid    세션 식별자 (UUID)
     * @return Refresh Token
     */
    public String createRefreshToken(Long userId, String sid) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(expiry)
            .claim(CLAIM_SID, sid)
            .id(UUID.randomUUID().toString())
            .signWith(jwtKeyProvider.getSigningKey())
            .compact();
    }

    /**
     * 토큰이 유효한지 검증합니다.
     * <p>
     * 서명, 만료시간, 형식을 검증하며 실패 시 예외를 발생시킵니다.
     * </p>
     *
     * @param token JWT 문자열
     * @throws JwtTokenException 토큰이 유효하지 않은 경우(만료/서명오류/형식오류 등)
     */
    public void validateToken(String token) {
        verifyAndParse(token);
    }

    /**
     * 토큰에서 Claims를 파싱하여 반환합니다.
     *
     * @param token JWT 문자열
     * @return Claims
     */
    public Claims parseClaims(String token) {
        return verifyAndParse(token);
    }

    /**
     * 토큰의 subject(sub)를 반환합니다. (userId)
     */
    public String getSubject(String token) {
        return parseClaims(token).getSubject();
    }

    /**
     * 토큰의 subject(sub)를 Long 타입의 userId로 반환합니다.
     *
     * @throws JwtTokenException subject(userId)가 숫자 형식이 아닌 경우
     */
    public Long getUserId(String token) {
        try {
            return Long.valueOf(getSubject(token));
        } catch (NumberFormatException e) {
            throw new JwtTokenException(JwtTokenErrorCode.INVALID_SUBJECT, e);
        }
    }

    /**
     * Access Token에 포함된 role Claim 값을 반환합니다.
     * <p>
     * role Claim은 Access Token 전용이며, Refresh Token에는 포함되지 않습니다.
     * </p>
     *
     * @param token JWT 문자열
     * @return role 문자열 (예: ROLE_ADMIN, ROLE_LECTURER, ROLE_STUDENT)
     * @throws JwtTokenException role Claim이 누락된 경우
     */
    public String getRole(String token) {
        String role = parseClaims(token).get(CLAIM_ROLE, String.class);
        if (role == null || role.isBlank()) {
            throw new JwtTokenException(JwtTokenErrorCode.MISSING_ROLE);
        }
        return role;
    }

    /**
     * Refresh Token에 포함된 세션 식별자(sid)를 반환합니다.
     *
     * <p>
     * sid는 서버에서 세션 단위로 Refresh Token을 관리하기 위한 값이며, Redis 등 저장소에서 세션 키를 찾는 데 사용됩니다.
     * </p>
     *
     * @param token JWT 문자열
     * @return 세션 식별자
     */
    public String getSessionId(String token) {
        String sid = parseClaims(token).get(CLAIM_SID, String.class);
        if (sid == null || sid.isBlank()) {
            throw new JwtTokenException(JwtTokenErrorCode.INVALID);
        }
        return sid;
    }

    /**
     * Refresh Token의 고유 식별자(jti)를 반환합니다.
     *
     * <p>
     * jti는 Refresh Token Rotation(RTR) 구현 시 토큰 재사용(reuse detection)을 감지하기 위해 사용됩니다.
     * </p>
     *
     * @param token JWT 문자열
     * @return JWT ID (jti)
     */
    public String getJti(String token) {
        String jti = parseClaims(token).getId();
        if (jti == null || jti.isBlank()) {
            throw new JwtTokenException(JwtTokenErrorCode.INVALID);
        }
        return jti;
    }



    private String requireToken(String token) {
        if (token == null || token.isBlank()) {
            throw new JwtTokenException(JwtTokenErrorCode.EMPTY);
        }
        return token.trim();
    }

    private Claims verifyAndParse(String token) {
        String normalizedToken = requireToken(token);

        try {
            return Jwts.parser()
                .verifyWith(jwtKeyProvider.getSigningKey())
                .build()
                .parseSignedClaims(normalizedToken)
                .getPayload();

        } catch (ExpiredJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.EXPIRED, e);
        } catch (SecurityException e) {
            throw new JwtTokenException(JwtTokenErrorCode.INVALID_SIGNATURE, e);
        } catch (MalformedJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.MALFORMED, e);
        } catch (UnsupportedJwtException e) {
            throw new JwtTokenException(JwtTokenErrorCode.UNSUPPORTED, e);
        } catch (JwtException | IllegalArgumentException e) {
            throw new JwtTokenException(JwtTokenErrorCode.INVALID, e);
        }
    }
}