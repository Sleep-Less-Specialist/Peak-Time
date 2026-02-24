package com.github.sleeplessspecialist.peaktime.global.common.querydsl;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import com.querydsl.jpa.impl.JPAQueryFactory;

import jakarta.persistence.EntityManager;
import lombok.RequiredArgsConstructor;

/**
 *  * <p>
 *  * JPAQueryFactory를 빈으로 등록하여 Repository/Custom Repository에서
 *  * 타입 안전한 QueryDSL 쿼리를 작성할 수 있도록 합니다.
 *  * </p>
 *
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 23.
 */
@Configuration
@RequiredArgsConstructor
public class QuerydslConfig {

	private final EntityManager entityManager;

	@Bean
	public JPAQueryFactory jpaQueryFactory() {
		return new JPAQueryFactory(entityManager);
	}
}