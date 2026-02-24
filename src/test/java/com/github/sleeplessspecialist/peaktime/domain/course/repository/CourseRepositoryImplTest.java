package com.github.sleeplessspecialist.peaktime.domain.course.repository;

import static org.assertj.core.api.Assertions.*;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.context.annotation.Import;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition.CourseSortBy;
import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseSearchCondition.SortDirection;
import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.global.common.querydsl.QuerydslConfig;
import com.github.sleeplessspecialist.peaktime.global.config.JpaAuditingTestConfig;

import jakarta.persistence.EntityManager;

/**
 * CourseRepositoryImplTest 입니다
 *
 * @author 기섭
 * @since 2026. 2. 24.
 */
@Testcontainers
@DataJpaTest
@Import({QuerydslConfig.class, JpaAuditingTestConfig.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class CourseRepositoryImplTest {

	@Container
	@ServiceConnection
	static MySQLContainer<?> mysql = new MySQLContainer<>("mysql:8.0")
		.withDatabaseName("testdb")
		.withUsername("test")
		.withPassword("test");

	@Autowired
	CourseRepository courseRepository;
	@Autowired
	EntityManager em;

	private User persistLecturer(String name) {
		String email = name + "_" + UUID.randomUUID() + "@test.com";
		User lecturer = User.createForSignup(name, email, "encoded-password", "01012345678");
		em.persist(lecturer);
		return lecturer;
	}

	private Course persistCourse(String title, String desc, String category, BigDecimal price, User lecturer) {
		Course course = Course.builder()
			.title(title)
			.description(desc)
			.category(category)
			.price(price)
			.thumbnailUrl(null)
			.lecturer(lecturer)
			.build();
		em.persist(course);
		return course;
	}

	@Test
	@DisplayName("카테고리 필터가 적용된다")
	void search_category_filter() {
		User lecturer = persistLecturer("lecturer");
		persistCourse("Spring", "desc", "BACKEND", new BigDecimal("10000"), lecturer);
		persistCourse("React", "desc", "FRONTEND", new BigDecimal("20000"), lecturer);
		em.flush();
		em.clear();

		CourseSearchCondition cond = new CourseSearchCondition(
			"BACKEND", null, null, null, CourseSortBy.CREATED_AT, SortDirection.DESC
		);

		Page<Course> result = courseRepository.searchCourses(cond, PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getCategory()).isEqualTo("BACKEND");
	}

	@Test
	@DisplayName("키워드 검색이 title/description에 대해 containsIgnoreCase로 적용된다")
	void search_keyword_filter() {
		User lecturer = persistLecturer("lecturer");
		persistCourse("Spring Boot", "JPA 포함", "BACKEND", new BigDecimal("10000"), lecturer);
		persistCourse("Node", "express", "BACKEND", new BigDecimal("10000"), lecturer);
		em.flush();
		em.clear();

		CourseSearchCondition cond = new CourseSearchCondition(
			null, "jpa", null, null, CourseSortBy.CREATED_AT, SortDirection.DESC
		);

		Page<Course> result = courseRepository.searchCourses(cond, PageRequest.of(0, 10));

		assertThat(result.getContent()).hasSize(1);
		assertThat(result.getContent().get(0).getTitle()).containsIgnoringCase("Spring");
	}

	@Test
	@DisplayName("가격 범위(min/max)가 적용된다")
	void search_price_range() {
		User lecturer = persistLecturer("lecturer");
		persistCourse("A", "desc", "BACKEND", new BigDecimal("10000"), lecturer);
		persistCourse("B", "desc", "BACKEND", new BigDecimal("30000"), lecturer);
		persistCourse("C", "desc", "BACKEND", new BigDecimal("50000"), lecturer);
		em.flush();
		em.clear();

		CourseSearchCondition cond = new CourseSearchCondition(
			null, null, new BigDecimal("20000"), new BigDecimal("40000"),
			CourseSortBy.PRICE, SortDirection.ASC
		);

		Page<Course> result = courseRepository.searchCourses(cond, PageRequest.of(0, 10));

		assertThat(result.getContent()).extracting(Course::getTitle).containsExactly("B");
	}

	@Test
	@DisplayName("가격 정렬 ASC/DESC가 적용된다")
	void search_sort_price_direction() {
		User lecturer = persistLecturer("lecturer");
		persistCourse("A", "desc", "BACKEND", new BigDecimal("10000"), lecturer);
		persistCourse("B", "desc", "BACKEND", new BigDecimal("30000"), lecturer);
		persistCourse("C", "desc", "BACKEND", new BigDecimal("20000"), lecturer);
		em.flush();
		em.clear();

		CourseSearchCondition asc = new CourseSearchCondition(
			null, null, null, null, CourseSortBy.PRICE, SortDirection.ASC
		);
		List<Course> ascList = courseRepository.searchCourses(asc, PageRequest.of(0, 10)).getContent();

		CourseSearchCondition desc = new CourseSearchCondition(
			null, null, null, null, CourseSortBy.PRICE, SortDirection.DESC
		);
		List<Course> descList = courseRepository.searchCourses(desc, PageRequest.of(0, 10)).getContent();

		assertThat(ascList).extracting(c -> c.getPrice().intValue())
			.containsExactly(10000, 20000, 30000);

		assertThat(descList).extracting(c -> c.getPrice().intValue())
			.containsExactly(30000, 20000, 10000);
	}

	@Test
	@DisplayName("페이징이 적용된다 (totalElements 포함)")
	void search_paging() {
		User lecturer = persistLecturer("lecturer");
		for (int i = 1; i <= 25; i++) {
			persistCourse("T" + i, "desc", "BACKEND", BigDecimal.valueOf(1000L * i), lecturer);
		}
		em.flush();
		em.clear();

		CourseSearchCondition cond = new CourseSearchCondition(
			null, null, null, null, CourseSortBy.PRICE, SortDirection.ASC
		);

		Page<Course> page0 = courseRepository.searchCourses(cond, PageRequest.of(0, 10));
		Page<Course> page1 = courseRepository.searchCourses(cond, PageRequest.of(1, 10));

		assertThat(page0.getContent()).hasSize(10);
		assertThat(page1.getContent()).hasSize(10);
		assertThat(page0.getTotalElements()).isEqualTo(25);
	}
}