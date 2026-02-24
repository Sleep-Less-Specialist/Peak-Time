package com.github.sleeplessspecialist.peaktime.domain.ranking.service;

import com.github.sleeplessspecialist.peaktime.domain.course.entity.Course;
import com.github.sleeplessspecialist.peaktime.domain.course.exception.CourseErrorCode;
import com.github.sleeplessspecialist.peaktime.domain.course.repository.CourseRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderItemRepository;
import com.github.sleeplessspecialist.peaktime.domain.order.repository.OrderRepository;
import com.github.sleeplessspecialist.peaktime.domain.ranking.dto.CourseRankingRes;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.RedisConnectionFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ZSetOperations.TypedTuple;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Duration;
import java.time.LocalDate;
import java.util.*;

/**
 * 강의 랭킹(인기 점수) 집계를 담당하는 서비스입니다.
 *
 * <p>
 * 강의의 인기 점수(조회/주문 등)는 Redis Sorted Set(ZSET)에 누적되며,
 * 최근 3일치 점수를 합산한 랭킹을 조회할 수 있습니다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.
 */

@Slf4j
@Service
@RequiredArgsConstructor
public class CourseRankingService {

    public static final String COURSE_DAILY_KEY_PREFIX = "rank:course:";
    private static final String COURSE_LAST3DAYS_KEY_PREFIX = "rank:course:last3days:";
    private static final Duration DAILY_KEY_TTL = Duration.ofDays(7);
    private static final Duration LAST3DAYS_CACHE_TTL = Duration.ofMinutes(2);

    private final StringRedisTemplate stringRedisTemplate;
    private final CourseRepository courseRepository;
    private final OrderRepository orderRepository;
    private final OrderItemRepository orderItemRepository;

    /**
     * 강의 인기 점수(조회수 등)를 Redis ZSET에 반영합니다.
     *
     * <p>
     * Redis Sorted Set(course)에 member로 강의 식별자(courseId 문자열)를 저장하고
     * score를 1 증가시킵니다.
     * </p>
     *
     */
    public void increaseCourseOrderCount(Long orderId) {

        List<Long> courseIds = orderItemRepository.findCourseIdsByOrderId(orderId);
        String key = COURSE_DAILY_KEY_PREFIX + LocalDate.now();

        for (Long courseId : courseIds.stream().distinct().toList()) {
            String member = String.valueOf(courseId);

            try {
                stringRedisTemplate.opsForZSet().incrementScore(key, member, 1D);
                log.info("반영 성공 key={},member={}", key, member);
            } catch (RedisConnectionFailureException e) {
                log.error("Redis 연결 장애로 점수 반영 실패. key={}, member(courseId)={}", key, member, e);
                throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
            } catch (Exception e) {
                log.error("Redis 오류로 점수 반영 실패. key={}, member(courseId)={}", key, member, e);
                throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
            }
        }
        try {
            stringRedisTemplate.expire(key, DAILY_KEY_TTL);
        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 장애로 TTL 설정 실패. key={}", key, e);
            throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
        } catch (Exception e) {
            log.error("Redis 오류로 TTL 설정 실패. key={}", key, e);
            throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
        }
    }


    /**
     * 최근 3일간의 인기 점수를 합산하여 상위 강의 목록을 반환합니다.
     *
     * <p>
     * 1) 최근 3일치 일별 ZSET을 union하여 "최근 3일 합산" ZSET을 만들고(캐시),
     * 2) 해당 ZSET에서 score 기준으로 상위 N개를 조회합니다.
     * 3) Redis member는 {@code courseId}이므로, DB에서 강의명을 조회해 응답 DTO에 매핑합니다.
     * </p>
     *
     * @return 상위 강의 랭킹 목록(강의명 + 점수)
     * @throws CustomException Redis 조회 실패 시
     */
    public List<CourseRankingRes> findTopCoursesInLast3Days() {

        LocalDate today = LocalDate.now();
        String key = COURSE_LAST3DAYS_KEY_PREFIX;

        try {
            buildLast3DaysZset(today, key);

            Set<TypedTuple<String>> tuples = stringRedisTemplate.opsForZSet().
                    reverseRangeWithScores(key, 0, 2);

            if (tuples == null || tuples.isEmpty()) {
                return Collections.emptyList();
            }

            List<Long> courseIds = extractCourseIds(tuples);

            if (courseIds.isEmpty()) {
                return Collections.emptyList();
            }

            Map<Long, String> idToTitle = loadCourseTitleMap(courseIds);

            return buildRankingResult(tuples, idToTitle);

        } catch (RedisConnectionFailureException e) {
            log.error("Redis 연결 장애로 랭킹 조회 실패. key={}", key, e);
            throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
        } catch (Exception e) {
            log.error("Redis 오류로 랭킹 조회 실패. key={}", key, e);
            throw new CustomException(CourseErrorCode.REDIS_UPDATE_FAILED);
        }
    }

    private void buildLast3DaysZset(LocalDate today, String last3DaysKey) {

        List<String> keys = List.of(
                COURSE_DAILY_KEY_PREFIX + today.toString(),
                COURSE_DAILY_KEY_PREFIX + today.minusDays(1).toString(),
                COURSE_DAILY_KEY_PREFIX + today.minusDays(2).toString()
        );
        stringRedisTemplate.opsForZSet().unionAndStore(
                keys.get(0),
                keys.subList(1, keys.size()),
                last3DaysKey
        );

        stringRedisTemplate.expire(last3DaysKey, LAST3DAYS_CACHE_TTL);
    }

    private List<Long> extractCourseIds(Set<TypedTuple<String>> tuples) {

        List<Long> courseIds = new ArrayList<>();

        for (TypedTuple<String> tuple : tuples) {
            if (tuple == null) {
                continue;
            }

            String value = tuple.getValue();
            if (value == null) {
                continue;
            }

            try {
                Long courseId = Long.parseLong(value);
                courseIds.add(courseId);
            } catch (NumberFormatException e) {
                log.warn("courseId 파싱 실패. member={}", value);
            }
        }

        return courseIds;
    }

    private Map<Long, String> loadCourseTitleMap(List<Long> courseIds) {

        List<Course> courses = courseRepository.findAllById(courseIds);

        Map<Long, String> idToTitle = new HashMap<>();

        for (Course course : courses) {
            idToTitle.put(course.getId(), course.getTitle());
        }

        return idToTitle;
    }

    private List<CourseRankingRes> buildRankingResult(
            Set<TypedTuple<String>> tuples,
            Map<Long, String> idToTitle
    ) {

        List<CourseRankingRes> result = new ArrayList<>();

        for (TypedTuple<String> tuple : tuples) {

            if (tuple == null || tuple.getValue() == null) {
                continue;
            }

            Long courseId;
            try {
                courseId = Long.parseLong(tuple.getValue());
            } catch (NumberFormatException e) {
                continue;
            }

            String courseName = idToTitle.get(courseId);
            if (courseName == null) {
                continue;
            }

            double score = tuple.getScore() == null ? 0D : tuple.getScore();

            result.add(new CourseRankingRes(courseName, score));
        }

        return result;
    }
}
