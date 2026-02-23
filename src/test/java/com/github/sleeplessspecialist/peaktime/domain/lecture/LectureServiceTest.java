package com.github.sleeplessspecialist.peaktime.domain.lecture;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.EnrollmentStatus;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.lecture.dto.LecturePlaybackRes;
import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;
import com.github.sleeplessspecialist.peaktime.domain.lecture.exception.LectureErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.lecture.repository.LectureRepository;
import com.github.sleeplessspecialist.peaktime.domain.lecture.service.LectureService;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.infra.s3.S3Uploader;

/**
 * LectureService 단위 테스트
 *
 * @author 기섭
 * @since 2026. 2. 23.
 */
@ExtendWith(MockitoExtension.class)
class LectureServiceTest {

	@Mock
	private LectureRepository lectureRepository;

	@Mock
	private CourseRepository courseRepository;

	@Mock
	private EnrollmentRepository enrollmentRepository;

	@Mock
	private S3Uploader s3Uploader;

	@InjectMocks
	private LectureService lectureService;

	@Test
	@DisplayName("ENROLLED 사용자는 200 + presigned URL을 반환한다 (S3Uploader mock)")
	void playback_enrolled_user_returns_presigned_url() {
		// given
		Long userId = 1L;
		Long lectureId = 10L;
		Long courseId = 100L;

		Lecture lecture = mock(Lecture.class);
		Course course = mock(Course.class);

		when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
		when(lecture.getCourse()).thenReturn(course);
		when(course.getId()).thenReturn(courseId);

		when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
			userId, courseId, EnrollmentStatus.ENROLLED
		)).thenReturn(true);
		
		when(lecture.getVideoUrl()).thenReturn("https://bucket.s3.ap-northeast-2.amazonaws.com/video/uuid_test.mp4");
		when(s3Uploader.getPresignedUrl("video/uuid_test.mp4")).thenReturn("https://signed-url");

		when(lecture.getId()).thenReturn(lectureId);
		when(lecture.getTitle()).thenReturn("테스트 강의");

		// when
		LecturePlaybackRes res = lectureService.getPlaybackInfo(userId, lectureId);

		// then
		assertThat(res).isNotNull();
		assertThat(res.lectureId()).isEqualTo(lectureId);
		assertThat(res.title()).isEqualTo("테스트 강의");
		assertThat(res.videoUrl()).isEqualTo("https://signed-url");

		verify(lectureRepository).findById(lectureId);
		verify(enrollmentRepository).existsByUserIdAndCourseIdAndStatus(userId, courseId, EnrollmentStatus.ENROLLED);
		verify(s3Uploader).getPresignedUrl("video/uuid_test.mp4");
	}

	@Test
	@DisplayName("미수강 사용자는 403(L004: NOT_ENROLLED_USER)을 반환한다")
	void playback_not_enrolled_user_throws_forbidden() {
		// given
		Long userId = 1L;
		Long lectureId = 10L;
		Long courseId = 100L;

		Lecture lecture = mock(Lecture.class);
		Course course = mock(Course.class);

		when(lectureRepository.findById(lectureId)).thenReturn(Optional.of(lecture));
		when(lecture.getCourse()).thenReturn(course);
		when(course.getId()).thenReturn(courseId);

		when(enrollmentRepository.existsByUserIdAndCourseIdAndStatus(
			userId, courseId, EnrollmentStatus.ENROLLED
		)).thenReturn(false);

		// when & then
		assertThatThrownBy(() -> lectureService.getPlaybackInfo(userId, lectureId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getErrorCode()).isEqualTo(LectureErrorCode.NOT_ENROLLED_USER);
				assertThat(ce.getErrorCode().getCode()).isEqualTo("L004");
			});

		verify(s3Uploader, never()).getPresignedUrl(any());
	}

	@Test
	@DisplayName("강의 미존재는 404(L003: LECTURE_NOT_FOUND)을 반환한다")
	void playback_lecture_not_found_throws_not_found() {
		// given
		Long userId = 1L;
		Long lectureId = 999L;

		when(lectureRepository.findById(lectureId)).thenReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> lectureService.getPlaybackInfo(userId, lectureId))
			.isInstanceOf(CustomException.class)
			.satisfies(ex -> {
				CustomException ce = (CustomException)ex;
				assertThat(ce.getErrorCode()).isEqualTo(LectureErrorCode.LECTURE_NOT_FOUND);
				assertThat(ce.getErrorCode().getCode()).isEqualTo("L003");
			});

		verify(enrollmentRepository, never()).existsByUserIdAndCourseIdAndStatus(any(), any(), any());
		verify(s3Uploader, never()).getPresignedUrl(any());
	}
}