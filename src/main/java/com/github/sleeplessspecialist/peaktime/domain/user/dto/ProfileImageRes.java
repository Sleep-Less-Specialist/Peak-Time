package com.github.sleeplessspecialist.peaktime.domain.user.dto;

import lombok.Builder;

/**
 * 프로필 이미지 업로드 요청이 성공했을 때,
 * 클라이언트에게 업로드된 이미지의 URL 정보를 반환하기 위해 사용됩니다.
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 2. 6.
 */
@Builder
public record ProfileImageRes(
	String imageUrl
) {
}
