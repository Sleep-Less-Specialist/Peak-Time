package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;

/**
 * CourseRepositoryCustom 인터페이스입니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 24.
 */
public interface CourseRepositoryCustom {
	Page<Course> searchCourses(CourseSearchCondition condition, Pageable pageable);
}
