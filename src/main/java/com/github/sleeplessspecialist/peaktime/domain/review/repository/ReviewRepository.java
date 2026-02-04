package com.github.sleeplessspecialist.peaktime.domain.review.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.review.entity.Review;

/**
 *
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */
public interface ReviewRepository extends JpaRepository<Review, Long> {
}
