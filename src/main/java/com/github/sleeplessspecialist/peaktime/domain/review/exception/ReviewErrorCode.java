package com.github.sleeplessspecialist.peaktime.domain.review.exception;

/**
 * 리뷰 (Review) 도메인에서 발생할 수 있는 에러 코드를 정의한 Enum.
 * <p>
 * GlobalExceptionHandler에서 이 코드를 기반으로 적절한 HTTP 상태 코드와 메시지를 반환한다.
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.02.03
 */

import com.github.sleeplessspecialist.peaktime.global.common.error.ErrorCode;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;

@Getter
@RequiredArgsConstructor
public enum ReviewErrorCode implements ErrorCode {

    USER_NOT_FOUND("RV001", "존재하지 않는 사용자입니다.", HttpStatus.NOT_FOUND),
    ENROLLMENT_NOT_FOUND("RV002", "존재하지 않는 수강 등록 입니다.", HttpStatus.NOT_FOUND),
    UNAUTHORIZED_ACCESS("RV002", "사용자 권한이 부족합니다.", HttpStatus.FORBIDDEN),
    REVIEW_ALREADY_EXISTS("RV003", "이미 리뷰가 존재 합니다.", HttpStatus.CONFLICT),
    REVIEW_NOT_FOUND("RV004", "존재하지 않는 리뷰입니다.", HttpStatus.NOT_FOUND);

    private final String code;
    private final String message;
    private final HttpStatus httpStatus;
}
