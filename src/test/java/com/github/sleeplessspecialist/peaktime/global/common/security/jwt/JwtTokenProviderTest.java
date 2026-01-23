package com.github.sleeplessspecialist.peaktime.global.common.security.jwt;


import static org.assertj.core.api.Assertions.*;
import static org.mockito.BDDMockito.*;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

/**
 * {@link JwtTokenProvider}에 대한 단위 테스트입니다.
 * <p>
 * Spring Context를 사용하지 않고, JWT 토큰 생성/검증/파싱 핵심 로직이 정상 동작하는지만 검증합니다.
 * </p>
 *
 * @author 재원
 * @since 2026. 1. 22.
 */
public class JwtTokenProviderTest {

	@Test
	@DisplayName("AccessToken 생성 및 검증 후 사용자 ID 추출 과정 테스트")
	void createAndParseAccessToken() {
		// given
		String rawSecret = "unit-test-secret-key-32bytes-minimum!!";
		String base64Secret = Base64.getEncoder()
			.encodeToString(rawSecret.getBytes(StandardCharsets.UTF_8));

		JwtProperties jwtProperties = Mockito.mock(JwtProperties.class);
		given(jwtProperties.getSecret()).willReturn(base64Secret);
		given(jwtProperties.getAccessTokenExpirationMs()).willReturn(60_000L);
		given(jwtProperties.getRefreshTokenExpirationMs()).willReturn(120_000L);

		JwtKeyProvider jwtKeyProvider = new JwtKeyProvider(jwtProperties);
		JwtTokenProvider jwtTokenProvider = new JwtTokenProvider(jwtProperties, jwtKeyProvider);

		Long userId = 1L;
		String role = "ROLE_USER";

		// when
		String accessToken = jwtTokenProvider.createAccessToken(userId, role);

		// then
		assertThat(accessToken).isNotBlank();

		// 토큰 검증 (예외가 발생하지 않아야 함)
		jwtTokenProvider.validateToken(accessToken);


		Long extractedUserId = jwtTokenProvider.getUserId(accessToken);
		assertThat(extractedUserId).isEqualTo(userId);

        String extractedRole = jwtTokenProvider.parseClaims(accessToken)
            .get(JwtTokenProvider.CLAIM_ROLE, String.class);
        assertThat(extractedRole).isEqualTo(role);
	}
}