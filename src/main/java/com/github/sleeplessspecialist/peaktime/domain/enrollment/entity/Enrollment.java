package com.github.sleeplessspecialist.peaktime.domain.enrollment.entity;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.domain.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * 수강 등록(Enrollment) 엔티티입니다.
 * <p>
 * 강의(course)와 수강생(student)의 관계를 저장하며,
 * 수강 상태(status), 수강 시작 시각(enrolledAt), 취소 시각(canceledAt)을 관리합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
	name = "enrollments",
	uniqueConstraints = {
		@UniqueConstraint(name = "uk_enrollment_course_user_id", columnNames = {"course_id", "user_id"})
	}
)
public class Enrollment extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Enumerated(EnumType.STRING)
	@Column(name = "status", nullable = false)
	private EnrollmentStatus status = EnrollmentStatus.ENROLLED;

	@Builder
	private Enrollment(Course course, User user) {
		this.course = course;
		this.user = user;
	}
}
