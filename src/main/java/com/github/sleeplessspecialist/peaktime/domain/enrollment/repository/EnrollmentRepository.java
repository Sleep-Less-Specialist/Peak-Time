package com.github.sleeplessspecialist.peaktime.domain.enrollment.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.EnrollmentStatus;

/**
 * 강의 수강 등록(enrollment) 엔티티에 대한 데이터 접근을 담당하는 Repository 인터페이스
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.29
 */
public interface EnrollmentRepository extends JpaRepository<Enrollment, Long> {

	List<Enrollment> findAllByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

	boolean existsByUserIdAndCourseIdIn(Long userId, List<Long> courseIds);

	List<Enrollment> findAllByUserIdAndStatusOrderByCreatedAtDesc(Long userId, EnrollmentStatus status);

	boolean existsByUserIdAndCourseIdAndStatus(Long userId, Long courseId, EnrollmentStatus status);
}
