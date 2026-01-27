package com.github.sleeplessspecialist.peaktime.domain.lecture.entity;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
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
 * 개별 강의 영상 정보를 저장하는 엔티티 클래스입니다.
 * <p>
 * 영상의 제목, URL, 재생 시간(Duration)을 관리하며
 * 특정 Course(강좌)에 소속됩니다.
 * </p>
 *
 * @author 기섭
 * @version 1.1
 * @since 2026. 1. 22.
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "lectures")
public class Lecture extends BaseTimeEntity {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String title;

	@Column(nullable = false)
	private String videoUrl;

	@Column(nullable = false)
	private Integer duration;

	@ManyToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "course_id", nullable = false)
	private Course course;

	@Builder
	public Lecture(String title, String videoUrl, Integer duration, Course course) {
		this.title = title;
		this.videoUrl = videoUrl;
		this.duration = (duration != null) ? duration : 0;
		this.course = course;
	}
}