package com.github.sleeplessspecialist.peaktime.domain.user.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 사용자 정보를 관리하는 엔티티 클래스입니다.
 * <p>
 * 서비스의 모든 회원(지식공유자, 수강생) 정보를 저장하며,
 * 강의 및 수강 신청 정보와 연관 관계를 맺습니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@Table(name = "users")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	private String name;

	@Column(unique = true,  nullable = false)
	private String email;
	// 테스트용 생성자 (필요 시 사용)
	public User(String name) {
		this.name = name;
	}
}