package com.github.sleeplessspecialist.peaktime.domain.course.entity;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;

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
 * 강의 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * 데이터베이스의 'courses' 테이블과 매핑되며,
 * 강의 제목, 설명, 가격, 썸네일 및 지식공유자 정보를 관리합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "courses")
public class Course {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(nullable = false)
	private String category;

	@Column(nullable = false)
	private BigDecimal price;

	private String thumbnailUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecturer_id", nullable = false)
	private User lecturer;

	private LocalDateTime createdAt;
	private LocalDateTime updatedAt;

	@Builder
	public Course(String title, String description, String category, BigDecimal price, String thumbnailUrl,
		User lecturer) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.price = price;
		this.thumbnailUrl = thumbnailUrl;
		this.lecturer = lecturer;
		this.createdAt = LocalDateTime.now();
		this.updatedAt = LocalDateTime.now();
	}
}
