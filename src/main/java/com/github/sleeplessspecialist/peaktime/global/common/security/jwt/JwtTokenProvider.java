package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;

import java.util.Date;

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
 * <p>
 * 본 클래스는 <b>토큰 자체</b>에 대한 책임만 가집니다.
 * 요청에서 토큰을 추출하고 SecurityContext에 인증 정보를 설정하는 역할은
 * 인증 필터(JwtAuthenticationFilter)에서 수행합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class JwtTokenProvider {

    /** 권한 정보를 담는 Claim 키 (Access Token 전용) */
    public static final String CLAIM_ROLE = "role";

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
     * <p>
     * Refresh Token에는 권한 정보를 포함하지 않습니다.
     * </p>
     *
     * @param userId 사용자 ID
     * @return Refresh Token
     */
    public String createRefreshToken(Long userId) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtProperties.getRefreshTokenExpirationMs());

        return Jwts.builder()
            .subject(String.valueOf(userId))
            .issuedAt(now)
            .expiration(expiry)
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