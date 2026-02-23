package com.github.sleeplessspecialist.peaktime.domain.lecture.dto;

import lombok.Builder;

/**
 * 강의 영상 재생을 위한 응답 DTO입니다.
 * <p>
 * 클라이언트에게 강의 제목과 보안 처리된 스트리밍 URL(Presigned URL)을 전달합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 6.
 */
@Builder
public record LecturePlaybackRes(
	Long lectureId,
	String title,
	String videoUrl
) {
}