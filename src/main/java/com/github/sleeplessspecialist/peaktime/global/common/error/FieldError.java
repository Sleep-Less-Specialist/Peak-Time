package com.github.sleeplessspecialist.peaktime.global.common.error;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 * Validation 실패 시 필드 단위 오류 정보를 표현하는 DTO입니다.
 *
 * <p>
 * 입력 값 검증 과정에서 발생한 오류를 필드별로 전달하기 위해 사용되며,
 * {@link ErrorResponse}의 errors 필드에 포함됩니다.
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Getter
@RequiredArgsConstructor
public final class FieldError {

	private final String field;
	private final String reason;
}
