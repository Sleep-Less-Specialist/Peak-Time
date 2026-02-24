package com.github.sleeplessspecialist.peaktime.domain.ranking.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 강의 인기 랭킹 조회 응답 DTO입니다.
 *
 * <p>
 * 최근 일정 기간(예: 3일) 동안 주문 수를 기반으로 집계된
 * 강의 이름과 점수(score)를 반환합니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.23
 */
@Getter
@AllArgsConstructor
public class CourseRankingRes {

    private String courseName;
    private double score;
}
