package com.github.sleeplessspecialist.peaktime.domain.review;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.review.dto.*;
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
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
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

    @Test
    @DisplayName("리뷰 상세 조회 성공: 본인 리뷰일 경우 상세 정보를 반환한다")
    void getReviewDetail_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;
        Long enrollmentId = 10L;
        Long courseId = 100L;
        int rating = 4;
        String content = "good";

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(courseId, lecturer);
        Enrollment enrollment = createEnrollment(enrollmentId, course, student);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, rating, content, now, now);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        GetReviewDetailRes result = reviewService.getReviewDetail(userId, reviewId);

        // then
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEnrollmentId()).isEqualTo(enrollmentId);
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getRating()).isEqualTo(rating);
        assertThat(result.getContent()).isEqualTo(content);
        assertThat(result.getCreatedAt()).isEqualTo(now);
        assertThat(result.getUpdatedAt()).isEqualTo(now);

        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 상세 조회 실패: 사용자 미존재 시 예외가 발생한다")
    void getReviewDetail_Fail_UserNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewDetail(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.USER_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 상세 조회 실패: 리뷰 미존재 시 예외가 발생한다")
    void getReviewDetail_Fail_ReviewNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewDetail(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 상세 조회 실패: 본인 리뷰가 아니면 권한 예외가 발생한다")
    void getReviewDetail_Fail_Unauthorized() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        User owner = createUser(2L, "owner@test.com");
        User lecturer = createUser(3L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(10L, course, owner);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, 5, "great", now, now);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.getReviewDetail(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 목록 조회 성공: 사용자 리뷰 목록과 페이지 정보를 반환한다")
    void getMyReviews_Success() {
        // given
        Long userId = 1L;
        int page = 1;
        int size = 2;

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);

        Enrollment enrollment1 = createEnrollment(10L, course, student);
        Enrollment enrollment2 = createEnrollment(11L, course, student);

        LocalDateTime now = LocalDateTime.now();
        Review review1 = createReview(1L, enrollment1, 5, "great", now, now);
        Review review2 = createReview(2L, enrollment2, 3, "ok", now.minusDays(1), now.minusDays(1));

        Pageable pageable = PageRequest.of(page - 1, size);
        Page<Review> reviewPage = new PageImpl<>(List.of(review1, review2), pageable, 5);

        given(userRepository.findById(userId)).willReturn(Optional.of(student));
        given(reviewRepository.findAllByEnrollment_User(eq(student), any(Pageable.class))).willReturn(reviewPage);

        // when
        GetReviewListRes result = reviewService.getMyReviews(userId, page, size);

        // then
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(size);
        assertThat(result.getTotalElements()).isEqualTo(5);
        assertThat(result.getTotalPages()).isEqualTo(3);
        assertThat(result.isHasNext()).isTrue();
        assertThat(result.getReviews()).hasSize(2);

        ReviewListItemRes item1 = result.getReviews().get(0);
        assertThat(item1.getReviewId()).isEqualTo(1L);
        assertThat(item1.getEnrollmentId()).isEqualTo(10L);
        assertThat(item1.getCourseId()).isEqualTo(100L);
        assertThat(item1.getRating()).isEqualTo(5);
        assertThat(item1.getContent()).isEqualTo("great");
        assertThat(item1.getCreatedAt()).isEqualTo(now);

        ReviewListItemRes item2 = result.getReviews().get(1);
        assertThat(item2.getReviewId()).isEqualTo(2L);
        assertThat(item2.getEnrollmentId()).isEqualTo(11L);
        assertThat(item2.getCourseId()).isEqualTo(100L);
        assertThat(item2.getRating()).isEqualTo(3);
        assertThat(item2.getContent()).isEqualTo("ok");
        assertThat(item2.getCreatedAt()).isEqualTo(now.minusDays(1));

        verify(userRepository).findById(userId);
        verify(reviewRepository).findAllByEnrollment_User(eq(student), any(Pageable.class));
    }

    @Test
    @DisplayName("리뷰 목록 조회 실패: 사용자 미존재 시 예외가 발생한다")
    void getMyReviews_Fail_UserNotFound() {
        // given
        Long userId = 1L;

        given(userRepository.findById(userId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.getMyReviews(userId, 1, 10))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.USER_NOT_FOUND);
        verify(userRepository).findById(userId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 수정 성공: 본인 리뷰일 경우 평점과 내용을 갱신한다")
    void updateReview_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;
        Long enrollmentId = 10L;
        Long courseId = 100L;

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(courseId, lecturer);
        Enrollment enrollment = createEnrollment(enrollmentId, course, student);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, 2, "old", now.minusDays(1), now.minusDays(1));

        UpdateReviewReq req = new UpdateReviewReq(5, "new");

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        UpdateReviewRes result = reviewService.updateReview(userId, reviewId, req);

        // then
        assertThat(result.getReviewId()).isEqualTo(reviewId);
        assertThat(result.getUserId()).isEqualTo(userId);
        assertThat(result.getEnrollmentId()).isEqualTo(enrollmentId);
        assertThat(result.getCourseId()).isEqualTo(courseId);
        assertThat(result.getRating()).isEqualTo(5);
        assertThat(result.getContent()).isEqualTo("new");
        assertThat(result.getCreatedAt()).isEqualTo(now.minusDays(1));
        assertThat(result.getUpdatedAt()).isEqualTo(now.minusDays(1));
        assertThat(review.getRating()).isEqualTo(5);
        assertThat(review.getContent()).isEqualTo("new");

        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 사용자 미존재 시 예외가 발생한다")
    void updateReview_Fail_UserNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;
        UpdateReviewReq req = new UpdateReviewReq(4, "ok");

        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(userId, reviewId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.USER_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 리뷰 미존재 시 예외가 발생한다")
    void updateReview_Fail_ReviewNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;
        UpdateReviewReq req = new UpdateReviewReq(4, "ok");

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(userId, reviewId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 수정 실패: 본인 리뷰가 아니면 권한 예외가 발생한다")
    void updateReview_Fail_Unauthorized() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;
        UpdateReviewReq req = new UpdateReviewReq(4, "ok");

        User owner = createUser(2L, "owner@test.com");
        User lecturer = createUser(3L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(10L, course, owner);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, 2, "old", now, now);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.updateReview(userId, reviewId, req))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 삭제 성공: 본인 리뷰일 경우 삭제가 수행된다")
    void deleteReview_Success() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        User student = createUser(userId, "student@test.com");
        User lecturer = createUser(2L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(10L, course, student);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, 5, "great", now, now);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when
        reviewService.deleteReview(userId, reviewId);

        // then
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
        verify(reviewRepository).delete(review);
    }

    @Test
    @DisplayName("리뷰 삭제 실패: 사용자 미존재 시 예외가 발생한다")
    void deleteReview_Fail_UserNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        given(userRepository.existsById(userId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.USER_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verifyNoInteractions(reviewRepository);
    }

    @Test
    @DisplayName("리뷰 삭제 실패: 리뷰 미존재 시 예외가 발생한다")
    void deleteReview_Fail_ReviewNotFound() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.empty());

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.REVIEW_NOT_FOUND);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
    }

    @Test
    @DisplayName("리뷰 삭제 실패: 본인 리뷰가 아니면 권한 예외가 발생한다")
    void deleteReview_Fail_Unauthorized() {
        // given
        Long userId = 1L;
        Long reviewId = 99L;

        User owner = createUser(2L, "owner@test.com");
        User lecturer = createUser(3L, "lecturer@test.com");
        Course course = createCourse(100L, lecturer);
        Enrollment enrollment = createEnrollment(10L, course, owner);

        LocalDateTime now = LocalDateTime.now();
        Review review = createReview(reviewId, enrollment, 5, "great", now, now);

        given(userRepository.existsById(userId)).willReturn(true);
        given(reviewRepository.findById(reviewId)).willReturn(Optional.of(review));

        // when & then
        assertThatThrownBy(() -> reviewService.deleteReview(userId, reviewId))
                .isInstanceOf(CustomException.class)
                .extracting("errorCode")
                .isEqualTo(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        verify(userRepository).existsById(userId);
        verify(reviewRepository).findById(reviewId);
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

    private Review createReview(Long id, Enrollment enrollment, int rating, String content, LocalDateTime createdAt, LocalDateTime updatedAt) {
        Review review = Review.builder()
                .enrollment(enrollment)
                .rating(rating)
                .content(content)
                .build();
        ReflectionTestUtils.setField(review, "id", id);
        ReflectionTestUtils.setField(review, "createdAt", createdAt);
        ReflectionTestUtils.setField(review, "updatedAt", updatedAt);
        return review;
    }
}
