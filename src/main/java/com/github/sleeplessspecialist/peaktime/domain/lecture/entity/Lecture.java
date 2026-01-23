package com.github.sleeplessspecialist.peaktime.domain.lecture.entity;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;

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
 * 개별 강의 영상 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * AWS S3에 업로드된 영상의 URL과 강의 제목을 관리하며,
 * 하나의 과정(Course)에 여러 개의 강의 영상(Lecture)이 소속되는 N:1 구조를 가집니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lectures")
public class Lecture {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String videoUrl;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Builder
	public Lecture(String title, String videoUrl, Course course) {
		this.title = title;
		this.videoUrl = videoUrl;
		this.course = course;
	}
}