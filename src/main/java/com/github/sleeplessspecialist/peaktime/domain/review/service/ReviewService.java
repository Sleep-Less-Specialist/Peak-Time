package com.github.sleeplessspecialist.peaktime.domain.review.service;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.entity.Enrollment;
import com.github.sleeplessspecialist.peaktime.domain.enrollment.repository.EnrollmentRepository;
import com.github.sleeplessspecialist.peaktime.domain.review.dto.*;
import com.github.sleeplessspecialist.peaktime.domain.review.entity.Review;
import com.github.sleeplessspecialist.peaktime.domain.review.exception.ReviewErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.review.repository.ReviewRepository;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import com.github.sleeplessspecialist.peaktime.domain.user.repository.UserRepository;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

/**
 * 리뷰 생성/변경과 같은 상태 변경 커맨드를 담당하는 서비스입니다.
 * <p>
 * 수강 이력(Enrollment)을 기준으로 리뷰를 생성하며,
 * 한 Enrollment 당 리뷰 1개 제약을 애플리케이션 레벨에서 선제 검증하고
 * DB 유니크 제약으로 최종 보장합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
@Service
@RequiredArgsConstructor
@Slf4j
public class ReviewService {

    private final ReviewRepository reviewRepository;
    private final EnrollmentRepository enrollmentRepository;
    private final UserRepository userRepository;

    /**
     * 리뷰 생성
     * 1. userId DB 존재 여부 확인
     * 2. enrollment 로 인가 검증
     * 3. 해당 course 의 가중치 갱신
     * 4. review 영속성 저장및 응답 dto 리턴
     */
    @Transactional
    public CreateReviewRes createReview(Long userId, Long enrollmentId, CreateReviewReq req) {

        validateUserExists(userId);

        Enrollment enrollment = getEnrollment(enrollmentId);

        validateEnrollmentOwner(userId, enrollment);

        Course course = enrollment.getCourse();

        Review review = Review.builder()
                .enrollment(enrollment)
                .rating(req.getRating())
                .content(req.getContent())
                .build();

        Review saved;
        try {
            saved = reviewRepository.save(review);
        } catch (DataIntegrityViolationException e) {
            log.debug("이미 review 가 존재 합니다. userId={}, enrollmentId={}", userId, enrollmentId);
            throw new CustomException(ReviewErrorCode.REVIEW_ALREADY_EXISTS);
        }

        course.updateRatingWeight(saved.getRating());

        return CreateReviewRes.builder()
                .id(saved.getId())
                .enrollmentId(enrollment.getId())
                .courseId(course.getId())
                .userId(userId)
                .rating(saved.getRating())
                .content(saved.getContent())
                .createdAt(saved.getCreatedAt())
                .updatedAt(saved.getUpdatedAt())
                .build();
    }

    /**
     * 리뷰 상세 조회
     * 1. userId DB 존재 여부 확인
     * 2. reviewId DB 존재 여부 확인
     * 3. review 로 인가 검증
     * 4. 응답 dto 리턴
     */
    @Transactional(readOnly = true)
    public GetReviewDetailRes getReviewDetail(Long userId, Long reviewId) {

        validateUserExists(userId);

        Review review = getReview(reviewId);

        validateReviewOwner(userId, review);

        return GetReviewDetailRes.builder()
                .reviewId(review.getId())
                .userId(userId)
                .enrollmentId(review.getEnrollment().getId())
                .courseId(review.getEnrollment().getCourse().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /**
     * 리뷰 목록 조회 (나의)
     * 1. userId DB 존재 여부 확인
     * 2. Pageable 객체 변환
     * 3. 쿼리메서드 호출후 응답 dto 로 리턴
     */
    @Transactional(readOnly = true)
    public GetReviewListRes getMyReviews(Long userId, int page, int size) {

        User user = getUser(userId);

        Pageable pageable = PageRequest.of(
                page - 1,
                size,
                Sort.by(Sort.Direction.DESC, "createdAt")
        );

        Page<Review> reviewPage = reviewRepository.findAllByEnrollment_User(user, pageable);

        List<ReviewListItemRes> items = reviewPage.getContent().stream()
                .map(ReviewListItemRes::from)
                .toList();

        return GetReviewListRes.builder()
                .reviews(items)
                .page(reviewPage.getNumber())
                .size(reviewPage.getSize())
                .totalElements(reviewPage.getTotalElements())
                .totalPages(reviewPage.getTotalPages())
                .hasNext(reviewPage.hasNext())
                .build();
    }

    /**
     * 리뷰 갱신
     * 1. userId DB 존재 여부 확인
     * 2. reviewId DB 존재 여부 확인
     * 3. review 로 인가 검증
     * 4. review update
     * 5. 응답 dto 리턴
     */
    @Transactional
    public UpdateReviewRes updateReview(Long userId, Long reviewId, UpdateReviewReq req) {

        validateUserExists(userId);

        Review review = getReview(reviewId);

        validateReviewOwner(userId, review);

        review.update(req.getRating(), req.getContent());

        return UpdateReviewRes.builder()
                .reviewId(review.getId())
                .userId(userId)
                .enrollmentId(review.getEnrollment().getId())
                .courseId(review.getEnrollment().getCourse().getId())
                .rating(review.getRating())
                .content(review.getContent())
                .createdAt(review.getCreatedAt())
                .updatedAt(review.getUpdatedAt())
                .build();
    }

    /**
     * 리뷰 삭제
     * 1. userId DB 존재 여부 확인
     * 2. reviewId DB 존재 여부 확인
     * 3. review 로 인가 검증
     * 4. review 삭제
     */
    @Transactional
    public void deleteReview(Long userId, Long reviewId) {

        validateUserExists(userId);

        Review review = getReview(reviewId);

        validateReviewOwner(userId, review);

        reviewRepository.delete(review);
    }

    private User getUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.USER_NOT_FOUND));
    }

    private Review getReview(Long reviewId) {
        return reviewRepository.findById(reviewId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.REVIEW_NOT_FOUND));
    }

    private void validateUserExists(Long userId) {
        if (!userRepository.existsById(userId)) {
            log.warn("리뷰 생성 실패 - 존재하지 않는 사용자. userId={}", userId);
            throw new CustomException(ReviewErrorCode.USER_NOT_FOUND);
        }
    }

    private void validateEnrollmentOwner(Long userId, Enrollment enrollment) {
        Long ownerId = enrollment.getUser().getId();
        if (!ownerId.equals(userId)) {
            log.warn(
                    "리뷰 생성 실패 - 권한 없음. userId={}, enrollmentId={}, ownerId={}",
                    userId,
                    enrollment.getId(),
                    ownerId
            );
            throw new CustomException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private void validateReviewOwner(Long userId, Review review) {
        Long ownerId = review.getEnrollment().getUser().getId();
        if (!ownerId.equals(userId)) {
            log.warn("리뷰 조회 실패 - 권한 없음. userId={}, reviewId={}, ownerId={}", userId, review.getId(), ownerId);
            throw new CustomException(ReviewErrorCode.UNAUTHORIZED_ACCESS);
        }
    }

    private Enrollment getEnrollment(Long enrollmentId) {
        return enrollmentRepository.findById(enrollmentId)
                .orElseThrow(() -> new CustomException(ReviewErrorCode.ENROLLMENT_NOT_FOUND));
    }

}
