package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;

import javax.crypto.SecretKey;

import org.springframework.stereotype.Component;

import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import lombok.extern.slf4j.Slf4j;

/**
 * JWT 서명에 사용할 {@link SecretKey}를 생성·제공하는 컴포넌트입니다.
 *
 * <h2>책임</h2>
 * <ul>
 *   <li>HS256 알고리즘에 맞는 서명 키 생성</li>
 *   <li>JwtTokenProvider로부터 Key 생성 책임 분리 (단일 책임 원칙)</li>
 *   <li>애플리케이션 초기화 시점에 1회 생성하여 재사용</li>
 * </ul>
 *
 * <h2>보안</h2>
 * <p>
 * secret 또는 Key 자체는 보안상 로그에 노출하지 않습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Component
@Slf4j
public class JwtKeyProvider {

	private final SecretKey signingKey;

	/**
	 * JWT 서명 키를 초기화합니다.
	 *
	 * @param jwtProperties JWT 설정 프로퍼티
	 * @throws IllegalStateException secret이 올바른 Base64 형식이 아닌 경우
	 */
	public JwtKeyProvider(JwtProperties jwtProperties) {
		byte[] keyBytes;

		try {
			keyBytes = Decoders.BASE64.decode(jwtProperties.getSecret());
		} catch (IllegalArgumentException e) {
			throw new IllegalStateException(
				"JWT secret 설정이 올바른 Base64 형식이 아닙니다. 설정 값(jwt.secret 또는 환경변수 JWT_SECRET_KEY)을 확인해주세요.",
				e
			);
		}

		this.signingKey = Keys.hmacShaKeyFor(keyBytes);

		// Key 내용은 절대 노출하지 않고, 길이/알고리즘 정보만 debug 레벨로 남깁니다.
		if (log.isDebugEnabled()) {
			log.debug(
				"JWT 인증키가 생성되었습니다. (algorithm=HS256, keyLength={} bytes)",
				keyBytes.length
			);
		}
	}

	/**
	 * JWT 서명에 사용할 {@link SecretKey}를 반환합니다.
	 *
	 * @return JWT 서명에 사용할 SecretKey (불변, 캐시됨)
	 */
	public SecretKey getSigningKey() {
		return signingKey;
	}
}

