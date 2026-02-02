package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
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
	 * 강의 상세 조회
	 * @EntityGraph를 사용하여 Lecturer(강사)와 Lectures(커리큘럼)를 한 번에 가져옵니다.
	 */
	@Override
	@EntityGraph(attributePaths = {"lecturer", "lectures"})
	Optional<Course> findById(Long id);

	/**
	 * 강의 목록 조회 (페이징)
	 * @EntityGraph를 사용하여 Lecturer(강사) 정보만 함께 가져옵니다.
	 */
	@Override
	@EntityGraph(attributePaths = {"lecturer"})
	Page<Course> findAll(Pageable pageable);

	/**
	 * List<Long> courseId 를 fetch join 으로 user 까지 가지고 오기
	 */
	@Query("""
	select c
    from Course c
    join fetch c.lecturer u
    where c.id in :ids""")
	List<Course> findAllByIdInWithUser(@Param("ids") List<Long> ids);

}