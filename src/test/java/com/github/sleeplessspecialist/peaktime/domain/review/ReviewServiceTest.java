package com.github.sleeplessspecialist.peaktime.domain.review;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.review.dto.CreateReviewReq;
import com.github.sleeplessspecialist.peaktime.domain.review.dto.CreateReviewRes;
import com.github.sleeplessspecialist.peaktime.domain.review.entity.Review;
import com.github.sleeplessspecialist.peaktime.domain.review.exception.ReviewErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.review.repository.ReviewRepository;
import com.github.sleeplessspecialist.peaktime.domain.review.service.ReviewService;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.*;

/**
 * ReviewService 단위 테스트
 * <p>
 * 리뷰(Review) 도메인의 생성 로직 및
 * 수강 이력(Enrollment) 기반 권한 검증,
 * 강의(Course) 평점/리뷰 수 갱신 로직을 검증한다.
 * </p>
 *
 * @author 주우재
 * @since 2026. 2. 4.
 */
@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @InjectMocks
    private ReviewService reviewService;

    @Mock
    private ReviewRepository reviewRepository;

    @Mock
    private EnrollmentRepository enrollmentRepository;

    @Mock
    private UserRepository userRepository;

    @Test
    @DisplayName("리뷰 생성 성공 시: 리뷰가 저장되고 강의 평점 및 리뷰 수가 갱신된다")
    void createReview_Success() {
        // given
        Long userId = 1L;
        Long enrollmentId = 10L;
        int rating = 5;
        String content = "Great course";

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(enrollmentId, course, student);

        CreateReviewReq req = new CreateReviewReq(rating, content);

        LocalDateTime now = LocalDateTime.now();

        given(userRepository.existsById(userId)).willReturn(true);
        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));
        given(reviewRepository.save(any(Review.class))).willAnswer(invocation -> {
            Review saved = invocation.getArgument(0);
            ReflectionTestUtils.setField(saved, "id", 999L);
            ReflectionTestUtils.setField(saved, "createdAt", now);
            ReflectionTestUtils.setField(saved, "updatedAt", now);
            return saved;
        });

        // when
        CreateReviewRes result = reviewService.createReview(userId, enrollmentId, req);

        // then
        assertThat(result.getId()).isEqualTo(999L);
        assertThat(result.getCourseId()).isEqualTo(100L);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getRating()).isEqualTo(rating);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);

        assertThat(course.getReviewCount()).isEqualTo(1);
        assertThat(course.getRatingAvg()).isEqualTo(5.0);

        verify(userRepository).existsById(userId);
        verify(enrollmentRepository).findById(enrollmentId);
        verify(reviewRepository).save(any(Review.class));
    }

    @Test
    @DisplayName("리뷰 생성 실패: 존재하지 않는 사용자일 경우 예외가 발생한다")
    void createReview_Fail_UserNotFound() {
        // given
        Long userId = 1L;
        Long enrollmentId = 10L;
        CreateReviewReq req = new CreateReviewReq(3, "ok");

        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, enrollmentId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.USER_NOT_FOUND);

        verify(userRepository).existsById(userId);
        verifyNoInteractions(enrollmentRepository, reviewRepository);
    }

    @Test
    @DisplayName("리뷰 생성 실패: 수강 이력이 존재하지 않을 경우 예외가 발생한다")
    void createReview_Fail_EnrollmentNotFound() {
        // given
        Long userId = 1L;
        Long enrollmentId = 10L;
        CreateReviewReq req = new CreateReviewReq(3, "ok");

        given(userRepository.existsById(userId)).willReturn(true);
        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, enrollmentId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.ENROLLMENT_NOT_FOUND);

        verify(userRepository).existsById(userId);
        verify(enrollmentRepository).findById(enrollmentId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 생성 실패: 수강 이력의 소유자가 아닌 경우 접근 권한 예외가 발생한다")
    void createReview_Fail_Unauthorized() {
        // given
        Long userId = 1L;
        Long enrollmentId = 10L;
        CreateReviewReq req = new CreateReviewReq(4, "good");

        User owner = createUser(2L, "owner@test.com");
        User lecturer = createUser(3L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(enrollmentId, course, owner);

        given(userRepository.existsById(userId)).willReturn(true);
        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, enrollmentId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.UNAUTHORIZED_ACCESS);

        verify(userRepository).existsById(userId);
        verify(enrollmentRepository).findById(enrollmentId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 생성 실패: 이미 리뷰가 존재할 경우 중복 리뷰 예외가 발생한다")
    void createReview_Fail_ReviewAlreadyExists() {
        // given
        Long userId = 1L;
        Long enrollmentId = 10L;
        CreateReviewReq req = new CreateReviewReq(2, "bad");

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(enrollmentId, course, student);

        given(userRepository.existsById(userId)).willReturn(true);
        given(enrollmentRepository.findById(enrollmentId)).willReturn(Optional.of(enrollment));
        given(reviewRepository.save(any(Review.class)))
                .willThrow(new DataIntegrityViolationException("dup"));

        // when & then
        assertThatThrownBy(() -> reviewService.createReview(userId, enrollmentId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.REVIEW_ALREADY_EXISTS);

        assertThat(course.getReviewCount()).isEqualTo(0);
        assertThat(course.getRatingAvg()).isEqualTo(0.0);

        verify(userRepository).existsById(userId);
        verify(enrollmentRepository).findById(enrollmentId);
        verify(reviewRepository).save(any(Review.class));
    }

    private User createUser(Long id, String email) {
        User user = User.createForSignup("name", email, "pw", "01012345678");
        ReflectionTestUtils.setField(user, "id", id);
        return user;
    }

    private Course createCourse(Long id, User lecturer) {
        Course course = Course.builder()
                .title("title")
                .description("desc")
                .category("BACKEND")
                .price(BigDecimal.valueOf(10000))
                .thumbnailUrl("thumb.jpg")
                .lecturer(lecturer)
                .build();
        ReflectionTestUtils.setField(course, "id", id);
        return course;
    }

    private Enrollment createEnrollment(Long id, Course course, User user) {
        Enrollment enrollment = Enrollment.builder()
                .course(course)
                .user(user)
                .build();
        ReflectionTestUtils.setField(enrollment, "id", id);
        return enrollment;
    }
}
