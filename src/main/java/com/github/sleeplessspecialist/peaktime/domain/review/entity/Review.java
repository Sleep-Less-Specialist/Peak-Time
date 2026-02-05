package com.github.sleeplessspecialist.peaktime.domain.review.entity;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 강의 수강(Enrollment)에 대한 리뷰 정보를 관리하는 엔티티입니다.
 *
 * <p>
 * 리뷰는 특정 수강 이력(Enrollment)에 종속되며, 한 Enrollment 당 하나의 Review만 작성할 수 있습니다.
 * 이를 DB 레벨에서 보장하기 위해 {@code enrollment_id} 컬럼에 유니크 제약을 둡니다.
 * 또한 좋아요/댓글 수는 조회 성능을 위해 집계 컬럼로 관리합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
@Entity
@Getter
@Table(
	name = "reviews",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_review_enrollment", columnNames = "enrollment_id")
	}
)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Review extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY, optional = false)
	@JoinColumn(name = "enrollment_id", nullable = false)
	private Enrollment enrollment;

	@Column(nullable = false)
	private int rating;

	@Lob
	@Column(nullable = false)
	private String content;

	@Builder
	public Review(Enrollment enrollment, int rating, String content) {
		this.enrollment = enrollment;
		this.rating = rating;
		this.content = content;
	}

    public void update(Integer rating, String content) {
        this.rating = rating;
        this.content = content;
    }

}
