package com.github.sleeplessspecialist.peaktime.domain.user.entity;

import java.util.ArrayList;
import java.util.List;

import com.github.sleeplessspecialist.peaktime.domain.point.exception.PointErrorCode;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
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
		@UniqueConstraint(name = "uk_users_email", columnNames = "email"),
		@UniqueConstraint(name = "uk_users_provider", columnNames = {"auth_provider", "provider_id"})
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class User extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 100)
	private String name;

	@Column(name = "password_hash", length = 200)
	private String passwordHash;

	@Column(nullable = false, length = 150)
	private String email;

	/**
	 * 인증 제공자(Local / OAuth2).
	 * <p>
	 * LOCAL  : 자체 회원가입 (이메일/비밀번호)
	 * KAKAO  : 카카오 OAuth2
	 * GOOGLE : 구글 OAuth2 (확장 예정)
	 * </p>
	 */
	@Column(name = "auth_provider", nullable = false, length = 20)
	private String authProvider;

	/**
	 * OAuth2 인증 제공자에서 발급하는 고유 사용자 식별자입니다.
	 * <p>
	 * LOCAL 회원가입 사용자는 null 입니다.
	 * </p>
	 */
	@Column(name = "provider_id", length = 50)
	private String providerId;

	@Column(name = "phone_number", length = 20)
	private String phoneNumber;

	@PositiveOrZero
	@Column(nullable = false)
	private Long point;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserRole role;

	@Enumerated(EnumType.STRING)
	@Column(nullable = false, length = 20)
	private UserStatus status;

	@OneToMany(mappedBy = "user", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<ProfileImage> profileImages = new ArrayList<>();

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
		user.authProvider = "LOCAL";
		user.providerId = null;
		user.passwordHash = passwordHash;
		user.phoneNumber = phoneNumber;
		user.point = 0L;
		user.role = UserRole.STUDENT;
		user.status = UserStatus.ACTIVE;

		return user;
	}

	/**
	 * OAuth2 로그인 사용자 엔티티를 생성합니다.
	 * <p>
	 * OAuth2 사용자는 최초 로그인 시점에 비밀번호 및 전화번호가 없을 수 있으며, 추가 정보 입력(Onboarding) 단계에서 보완됩니다.
	 * </p>
	 */
	public static User createForOAuth2(
		String name,
		String email,
		String provider,
		String providerId
	) {
		User user = new User();
		user.name = name;
		user.email = email;
		user.authProvider = provider;
		user.providerId = providerId;
		user.passwordHash = null;
		user.phoneNumber = null;
		user.point = 0L;
		user.role = UserRole.STUDENT;
		user.status = UserStatus.ACTIVE;

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

	public void changeStatus(UserStatus newStatus) {
		if (this.status == newStatus) {
			return;
		}
		this.status = newStatus;
	}

	// ✅ 추가: 프로필 정보 수정 (이름, 전화번호)
	public void updateProfile(String name, String phoneNumber) {
		if (name != null && !name.isBlank()) {
			this.name = name;
		}
		if (phoneNumber != null && !phoneNumber.isBlank()) {
			this.phoneNumber = phoneNumber;
		}
	}

	// ✅ 편의 메서드: 현재 대표 이미지 URL 가져오기 (DTO 변환용)
	public String getProfileImageUrl() {
		return this.profileImages.stream()
			.filter(ProfileImage::isPrimary)
			.findFirst()
			.map(ProfileImage::getUrl)
			.orElse(null); // 이미지가 없으면 null 반환
	}
}