package com.github.sleeplessspecialist.peaktime.domain.review.repository;

import com.github.sleeplessspecialist.peaktime.domain.review.entity.Review;
import com.github.sleeplessspecialist.peaktime.domain.user.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

/**
 * 리뷰(Review) 엔티티에 대한 영속성 처리를 담당하는 리포지토리입니다.
 * <p>
 * 리뷰는 수강 이력(Enrollment)에 종속 된다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {

    Page<Review> findAllByEnrollment_User(User user, Pageable pageable);
}
