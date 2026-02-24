package com.github.sleeplessspecialist.peaktime.domain.course.controller;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Page;
import org.springframework.test.web.servlet.MockMvc;

import com.github.sleeplessspecialist.peaktime.domain.course.dto.CourseListRes;
import com.github.sleeplessspecialist.peaktime.domain.course.service.CourseService;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false) // ✅ Security 필터만 끔 (302/401 방지)
class CourseControllerIntegrationTest {

	@Autowired
	MockMvc mockMvc;

	@MockBean
	CourseService courseService;

	@Test
	@DisplayName("minPrice > maxPrice면 400 + C005 + 메시지를 반환한다")
	void minPriceGreaterThanMaxPrice_returns400_C005() throws Exception {
		mockMvc.perform(get("/api/v1/courses")
				.param("minPrice", "50000")
				.param("maxPrice", "10000"))
			.andExpect(status().isBadRequest())
			.andExpect(jsonPath("$.code").value("C005"))
			.andExpect(jsonPath("$.message").value("최소 가격은 최대 가격보다 클 수 없습니다."));
	}

	@Test
	@DisplayName("minPrice만 존재하면 정상(200) 응답한다")
	void onlyMinPrice_ok() throws Exception {
		given(courseService.getCourseList(any(), any()))
			.willReturn(Page.<CourseListRes>empty());

		mockMvc.perform(get("/api/v1/courses")
				.param("minPrice", "10000"))
			.andExpect(status().isOk());
	}

	@Test
	@DisplayName("maxPrice만 존재하면 정상(200) 응답한다")
	void onlyMaxPrice_ok() throws Exception {
		given(courseService.getCourseList(any(), any()))
			.willReturn(Page.<CourseListRes>empty());

		mockMvc.perform(get("/api/v1/courses")
				.param("maxPrice", "50000"))
			.andExpect(status().isOk());
	}
}