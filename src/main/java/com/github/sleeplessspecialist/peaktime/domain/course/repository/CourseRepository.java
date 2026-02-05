package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;

/**
 * 강의 엔티티의 데이터베이스 접근 및 영속성 관리를 담당하는 리포지토리입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
public interface CourseRepository extends JpaRepository<Course, Long> {

	/**
	 * List<Long> courseId 를 fetch join 으로 user 까지 가지고 오기
	 */
	@Query("""
		select c
		   from Course c
		   join fetch c.lecturer u
		   where c.id in :ids""")
	List<Course> findAllByIdInWithUser(@Param("ids") List<Long> ids);

	/**
	 * 특정 강사의 강의 목록을 페이징하여 조회
	 */
	Page<Course> findAllByLecturerId(Long lecturerId, Pageable pageable);
}