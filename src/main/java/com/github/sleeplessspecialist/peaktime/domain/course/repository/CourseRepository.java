package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;

/**
 * 강의(Course) 엔티티의 데이터베이스 접근 및 영속성 관리를 담당하는 리포지토리입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

	/**
	 * 강의 ID로 강의 상세 정보를 조회합니다.
	 * 지식공유자(lecturer)와 커리큘럼(lectures) 정보를 한 번의 쿼리로 함께 가져옵니다
	 */
	@Query("SELECT c FROM Course c " +
		"JOIN FETCH c.lecturer " +
		"LEFT JOIN FETCH c.lectures " +
		"WHERE c.id = :courseId")
	Optional<Course> findByIdWithDetail(@Param("courseId") Long courseId);
}