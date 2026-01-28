package com.github.sleeplessspecialist.peaktime.domain.user.entity;

import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * ProfileImage 클래스입니다.
 * <p>
 * 사용자의 프로필 이미지 정보를 관리하는 엔티티 클래스입니다.
 * 이미지 URL, 대표 이미지 여부(isPrimary)를 저장하며 User 엔티티와 N:1 연관 관계를 맺습니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 28.
 */
@Entity
@Getter
@Table(name = "profile_images")
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class ProfileImage extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false, length = 500)
	private String url;

	@Column(name = "is_primary", nullable = false)
	private boolean isPrimary;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id")
	private User user;

	@Builder
	public ProfileImage(String url, boolean isPrimary, User user) {
		this.url = url;
		this.isPrimary = isPrimary;
		this.user = user;
	}

	/**
	 * 해당 이미지의 대표 이미지 여부를 설정합니다.
	 *
	 * @param isPrimary true면 대표 이미지, false면 일반 이미지
	 */
	public void setPrimary(boolean isPrimary) {
		this.isPrimary = isPrimary;
	}
}