package com.github.sleeplessspecialist.peaktime.domain.lecture.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import com.github.sleeplessspecialist.peaktime.domain.lecture.entity.Lecture;

/**
 * 강의 영상(Lecture) 엔티티의 데이터베이스 접근 및 영속성 관리를 담당하는 리포지토리입니다.
 * <p>
 * 영상 정보의 조회, 저장, 삭제 등의 기본적인 CRUD 기능을 제공합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 22.
 */
public interface LectureRepository extends JpaRepository<Lecture, Long> {
}