package com.github.sleeplessspecialist.peaktime.global.config.swagger;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;

/**
 * Swagger(OpenAPI) 문서 설정 클래스입니다.
 *
 * <p>
 * - Peak-Time API의 기본 문서 정보(title, version, description)를 정의합니다.
 * - JWT 기반 Bearer 인증 방식을 Swagger 문서에 반영합니다.
 * - Swagger UI의 Authorize 버튼을 통해 JWT 토큰을 입력하여
 *   보호된 API를 테스트할 수 있도록 보안 스키마를 설정합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026. 02. 26
 */
@Configuration
@OpenAPIDefinition(
        info = @Info(
                title = "Peak-Time API",
                version = "v1",
                description = "Peak-Time REST API documentation"
        ),
        security = @SecurityRequirement(name = "bearerAuth")
)
@SecurityScheme(
        name = "bearerAuth",
        type = SecuritySchemeType.HTTP,
        scheme = "bearer",
        bearerFormat = "JWT"
)
public class OpenApiConfig {

    /**
     * OpenAPI 문서의 추가 메타 정보를 설정합니다.
     *
     * <p>
     * API 문서의 제목, 버전, 설명, 연락처 정보를 정의합니다.
     * Swagger UI 상단에 표시되는 기본 정보를 구성하는 역할을 합니다.
     * </p>
     *
     * @return OpenAPI 설정 객체
     */
    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new io.swagger.v3.oas.models.info.Info()
                        .title("Peak-Time API")
                        .version("v1")
                        .description("Peak-Time REST API documentation")
                        .contact(new Contact().name("Peak-Time Team")));
    }
}