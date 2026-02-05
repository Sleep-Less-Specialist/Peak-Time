package com.github.sleeplessspecialist.peaktime.domain.course.entity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 정보를 저장하는 엔티티 클래스입니다.
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "courses")
public class Course extends BaseTimeEntity {

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

	@Column(nullable = false)
	private Double ratingAvg = 0.0;

	@Column(nullable = false)
	private Integer reviewCount = 0;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "lecturer_id", nullable = false)
	private User lecturer;

	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Lecture> lectures = new ArrayList<>();

	@Builder
	public Course(String title, String description, String category, BigDecimal price, String thumbnailUrl,
		User lecturer) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.price = price;
		this.thumbnailUrl = thumbnailUrl;
		this.lecturer = lecturer;
	}

	public void updateRatingWeight(int newRating) {
		if (this.reviewCount == null)
			this.reviewCount = 0;
		if (this.ratingAvg == null)
			this.ratingAvg = 0.0;

		this.reviewCount += 1;

		int n = this.reviewCount;
		double oldAvg = this.ratingAvg;

		this.ratingAvg = ((oldAvg * (n - 1)) + newRating) / n;
	}

	/**
	 * 강의 정보 수정
	 */
	public void update(String title, String description, String category, BigDecimal price, String thumbnailUrl) {
		this.title = title;
		this.description = description;
		this.category = category;
		this.price = price;
		this.thumbnailUrl = thumbnailUrl;
	}
}