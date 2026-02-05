package com.github.sleeplessspecialist.peaktime.global.common.security.oauth2;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.github.sleeplessspecialist.peaktime.domain.point.service.PointService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * OAuth2 로그인 과정에서 외부 인증 제공자(provider)의 사용자 정보를 조회하는 서비스입니다.
 * <p>
 * Spring Security OAuth2 로그인 플로우 중 {@link #loadUser(OAuth2UserRequest)}가 호출되며,
 * 현재 단계에서는 provider 조회가 정상적으로 동작하는지 확인하기 위한 최소 골격 구현만 포함합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 30.
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class CustomOAuth2UserService extends DefaultOAuth2UserService {

	private final UserRepository userRepository;
	private final PointService pointService;

	/**
	 * OAuth2 인증 제공자(provider)로부터 사용자 정보를 조회합니다.
	 * <p>
	 * Spring Security OAuth2 로그인 플로우 중, 인가 코드 교환 이후 호출되며
	 * {@link DefaultOAuth2UserService#loadUser(OAuth2UserRequest)}를 통해
	 * provider의 userInfo API를 호출합니다.
	 * </p>
	 *
	 * <p>
	 * 이후 단계에서 registrationId(kakao, google 등)에 따라 사용자 정보를 파싱하고,
	 * 서비스 도메인(User)으로 매핑하는 로직이 이 메서드에 추가될 예정입니다.
	 * </p>
	 *
	 * @param userRequest OAuth2 인증 요청 정보 (client registration, access token 등)
	 * @return OAuth2 인증 제공자로부터 조회된 사용자 정보
	 * @throws OAuth2AuthenticationException OAuth2 사용자 정보 조회 중 오류 발생 시
	 */
	@Override
	public OAuth2User loadUser(OAuth2UserRequest userRequest)
		throws OAuth2AuthenticationException {

		OAuth2User oAuth2User = super.loadUser(userRequest);

		String registrationId =
			userRequest.getClientRegistration().getRegistrationId();

		// 1. OAuth2 provider 확인 (Kakao / Google 지원)
		if (!"kakao".equals(registrationId) && !"google".equals(registrationId)) {
			throw new OAuth2AuthenticationException("지원하지 않는 OAuth2 연결입니다: " + registrationId);
		}

		// 2. Provider별 userInfo 응답 파싱
		Map<String, Object> attributes = oAuth2User.getAttributes();

		String providerId;
		String email;
		String nickname;
		String provider;

		// Kakao
		if ("kakao".equals(registrationId)) {
			// Kakao 고유 사용자 ID (providerId)
			providerId = String.valueOf(attributes.get("id"));

			// kakao_account 영역
			@SuppressWarnings("unchecked")
			Map<String, Object> kakaoAccount = (Map<String, Object>) attributes.get("kakao_account");

			email = null;
			nickname = null;

			if (kakaoAccount != null) {
				email = (String)kakaoAccount.get("email");

				@SuppressWarnings("unchecked")
				Map<String, Object> profile = (Map<String, Object>)kakaoAccount.get("profile");

				if (profile != null) {
					nickname = (String)profile.get("nickname");
				}
			}

			provider = "KAKAO";

			log.info(
				"카카오 OAuth2 사용자 정보를 불러왔습니다. providerId={}, email={}, nickname={}",
				providerId, email, nickname
			);
		}

		// Google
		else {
			// Google 고유 사용자 ID (providerId): 보통 'sub'
			Object sub = attributes.get("sub");
			providerId = (sub != null) ? String.valueOf(sub) : String.valueOf(attributes.get("id"));

			email = (String)attributes.get("email");

			// Google은 보통 name / given_name 등을 제공
			String name = (String)attributes.get("name");
			String givenName = (String)attributes.get("given_name");
			nickname = (name != null && !name.isBlank()) ? name : givenName;

			provider = "GOOGLE";

			log.info(
				"구글 OAuth2 사용자 정보를 불러왔습니다. providerId={}, email={}, nickname={}",
				providerId, email, nickname
			);
		}

		final String resolvedNickname = (nickname != null && !nickname.isBlank())
			? nickname
			: (provider + "User");
		final String resolvedEmail = email;

		User user = userRepository
			.findByAuthProviderAndProviderId(provider, providerId)
			.orElseGet(() -> {
				if (resolvedEmail != null && !resolvedEmail.isBlank()) {
					Optional<User> byEmail = userRepository.findByEmail(resolvedEmail);
					if (byEmail.isPresent()) {
						User existing = byEmail.get();

						log.info(
							"기존 계정에 OAuth 자동 연동을 수행합니다. email={}, provider={}, providerId={}",
							resolvedEmail, provider, providerId
						);

						existing.updateOAuthProvider(provider, providerId);

						return userRepository.save(existing);
					}
				}

				User created = userRepository.save(
					User.createForOAuth2(
						resolvedNickname,
						resolvedEmail,
						provider,
						providerId
					)
				);

				// OAuth 신규 가입도 로컬 회원가입과 동일하게 보너스 지급
				pointService.grantSignupBonus(created);

				return created;
			});

		Map<String, Object> merged = new HashMap<>(attributes);
		merged.put("userId", String.valueOf(user.getId()));
		merged.put("authProvider", user.getAuthProvider());

		return new DefaultOAuth2User(
			Collections.singleton(new SimpleGrantedAuthority("ROLE_" + user.getRole().name())),
			merged,
			"userId"
		);
	}
}