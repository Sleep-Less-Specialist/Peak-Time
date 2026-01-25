package com.github.sleeplessspecialist.peaktime.global.common.security.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Profile;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

/**
 * JpaAuditing 설정 클래스입니다.
 * <p>
 * 엔티티 생성/수정 시각 등 감사(Auditing) 필드 자동 주입을 위해 {@code @EnableJpaAuditing}을 활성화합니다.
 * Application 클래스에 직접 선언된 Auditing 설정을 분리하여 설정 책임을 명확히 하고,
 * 테스트(WebMvcTest) 환경에서의 컨텍스트 충돌 가능성을 줄이기 위해 클래스를 별도로 구성했습니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 25.
 */
@Profile("!test")
@Configuration
@EnableJpaAuditing
public class JpaAuditingConfig {

}