package com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response;

import java.util.List;

/**
 * 관리자 사용자(User) 목록 조회 응답 레코드 클래스입니다.
 * <p>
 * 관리자 사용자 목록 조회 API의 응답 데이터로,
 * Page 기반 페이징 정보와 사용자 요약 목록을 함께 제공합니다.
 * </p>
 *
 * @param content        사용자 요약 목록 데이터
 * @param page           현재 페이지 번호(0부터 시작)
 * @param size           페이지 크기
 * @param totalElements  전체 사용자 수
 * @param totalPages     전체 페이지 수
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
public record AdminUserListRes(
	List<AdminUserSummary> content,
	int page,
	int size,
	long totalElements,
	int totalPages
) {}
