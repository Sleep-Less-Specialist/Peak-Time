package com.github.sleeplessspecialist.peaktime.domain.user.entity;

import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.Positive;
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
 * <p>
 * email은 로그인 식별자로 사용되며,
 * 동시성 환경에서도 중복 계정 생성을 방지하기 위해
 * DB 레벨에서 유니크 제약을 테이블 단위로 관리합니다.
 * </p>
 *
 * @author 기섭, 재원
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@Table(
	name = "users",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_users_email", columnNames = "email")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "password_hash", nullable = false, length = 200)
	private String passwordHash;

	@Column(nullable = false)
	private String email;

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@Positive
	@Column(nullable = false)
	private Long point = 0L;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(nullable = false, length = 20)
	private String status;

}