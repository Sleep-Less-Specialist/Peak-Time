package com.github.sleeplessspecialist.peaktime.domain.user.service.admin;

import java.time.format.DateTimeFormatter;

import java.util.List;
import java.util.stream.Collectors;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response.AdminUserListRes;
import com.github.sleeplessspecialist.peaktime.domain.user.dto.admin.response.AdminUserSummary;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

/**
 * 관리자 사용자(User) 관리 유스케이스를 처리하는 서비스입니다.
 *
 * <p>
 * 관리자 전용 사용자 목록 조회, 사용자 상태 변경 등 관리 기능을 담당합니다.
 * 본 서비스는 엔티티를 직접 노출하지 않고, 관리자 전용 DTO로 변환하여 반환합니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class AdminUserService {

	private final UserRepository userRepository;

	/**
	 * 관리자 권한으로 사용자(User) 목록을 페이징 조회합니다.
	 * <p>
	 * 기본 조회 구현으로, 주어진 {@code page}/{@code size}에 따라 {@link UserRepository#findAll(Pageable)}을 호출하여
	 * 사용자 목록을 조회하고 {@link AdminUserSummary} 목록으로 변환한 뒤 {@link AdminUserListRes}로 반환합니다.
	 * </p>
	 *
	 * <p>
	 * NOTE: 입력값(page/size)의 기본값 적용 및 유효성 검증은 컨트롤러 계층에서 수행합니다.
	 * (예: page=0, size=20, size 범위 1~100)
	 * </p>
	 *
	 * @param page 조회 페이지 번호(0부터 시작)
	 * @param size 페이지 크기(1~100)
	 * @return 사용자 목록 조회 결과(페이징 메타데이터 포함)
	 */
	@Transactional(readOnly = true)
	public AdminUserListRes getUsers(int page, int size) {
		final Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
		final Page<User> result = userRepository.findAll(pageable);

		final List<AdminUserSummary> content = result.getContent().stream()
			.map(this::toSummary)
			.collect(Collectors.toList());

		return new AdminUserListRes(
			content,
			result.getNumber(),
			result.getSize(),
			result.getTotalElements(),
			result.getTotalPages()
		);
	}

	private AdminUserSummary toSummary(User user) {
		String createdAt = null;
		if (user.getCreatedAt() != null) {
			createdAt = user.getCreatedAt().format(DateTimeFormatter.ISO_LOCAL_DATE_TIME);
		}

		String role = null;
		if (user.getRole() != null) {
			role = user.getRole().name();
		}

		String status = null;
		if (user.getStatus() != null) {
			status = user.getStatus().name();
		}

		return new AdminUserSummary(
			user.getId(),
			user.getEmail(),
			user.getName(),
			role,
			status,
			createdAt
		);
	}

}