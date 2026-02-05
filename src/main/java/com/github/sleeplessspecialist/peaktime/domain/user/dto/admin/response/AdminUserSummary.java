package com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response;

/**
 * 관리자 사용자(User) 목록 조회 시 사용되는 사용자 요약 응답 레코드입니다.
 *
 * <p>
 * 관리자 사용자 목록 조회 API 응답의 {@code content[]} 요소로 사용되며,
 * 관리자 화면의 사용자 목록 테이블에 노출되는 최소한의 사용자 정보를 표현합니다.
 * </p>
 * <p>
 * Endpoint: {@code GET /api/v1/admin/users}
 * </p>
 *
 * @param memberId  사용자 ID
 * @param email     사용자 이메일
 * @param name      사용자 이름
 * @param role      사용자 역할(STUDENT | LECTURER | ADMIN)
 * @param status    사용자 상태(ACTIVE | SUSPENDED | DELETED)
 * @param createdAt 가입 일시(ISO-8601 문자열)
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
public record AdminUserSummary(
	Long memberId,
	String email,
	String name,
	String role,
	String status,
	String createdAt
) {}