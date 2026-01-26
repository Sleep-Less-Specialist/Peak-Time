package com.github.sleeplessspecialist.peaktime.domain.user.entity;

import com.github.sleeplessspecialist.peaktime.domain.point.exception.PointErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import jakarta.validation.constraints.PositiveOrZero;
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

	@Column(nullable = false, length = 150)
	private String email;

	@Column(name = "phone_number", nullable = false, length = 20)
	private String phoneNumber;

	@PositiveOrZero
	@Column(nullable = false)
	private Long point;

	@Column(nullable = false, length = 20)
	private String role;

	@Column(nullable = false, length = 20)
	private String status;

	/**
	 * 회원가입용 사용자 엔티티를 생성합니다.
	 *
	 * <p>
	 * 기본 초기값으로 point는 0, role은 STUDENT, status는 ACTIVE로 설정합니다.
	 * 회원가입 보너스 포인트 지급/이력 기록은 별도 포인트 서비스에서 처리합니다.
	 * </p>
	 *
	 * @param name         사용자 이름
	 * @param email        로그인 식별자(이메일)
	 * @param passwordHash 암호화된 비밀번호 해시
	 * @param phoneNumber  정규화된 휴대폰 번호(숫자만)
	 * @return 초기화된 사용자 엔티티
	 */
	public static User createForSignup(String name, String email, String passwordHash, String phoneNumber) {
		User user = new User();
		user.name = name;
		user.email = email;
		user.passwordHash = passwordHash;
		user.phoneNumber = phoneNumber;
		user.point = 0L;
		user.role = "STUDENT";
		user.status = "ACTIVE";

		return user;
	}

	/**
	 * 사용자 포인트 잔액을 증감합니다.
	 *
	 * <p>
	 * amount는 적립(+) 또는 차감(-) 모두 허용합니다.
	 * 포인트 잔액은 0 미만이 될 수 없습니다.
	 * </p>
	 *
	 * @param amount 증감할 포인트 값
	 * @throws CustomException 잔액이 0 미만이 되는 경우
	 */
	public void addPoint(long amount) {
		long updated = this.point + amount;
		if (updated < 0) {
			throw new CustomException(PointErrorCode.INSUFFICIENT_POINT);
		}
		this.point = updated;
	}

}