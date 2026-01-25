package com.github.sleeplessspecialist.peaktime.global.common.error;

import java.util.List;

import lombok.extern.slf4j.Slf4j;

import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

/**
 * 애플리케이션 전역에서 발생하는 예외를 공통 실패 응답({@link ErrorResponse})으로 변환하는 핸들러입니다.
 * <p>
 * - 비즈니스 예외: {@link CustomException}
 * - 입력 값 검증 예외: {@link MethodArgumentNotValidException}
 * - 그 외 예외: {@link Exception}
 * </p>
 *
 * @author 재원
 * @version 1.0
 * @since 2026. 1. 21.
 */
@Slf4j
@RestControllerAdvice
public class GlobalExceptionHandler {

	@ExceptionHandler(CustomException.class)
	public ResponseEntity<ErrorResponse> handleCustomException(CustomException e) {
		ErrorCode errorCode = e.getErrorCode();
		log.warn("비즈니스 예외 발생: code={}, message={}",
			errorCode.getCode(), errorCode.getMessage());
		return ResponseEntity
			.status(errorCode.getHttpStatus())
			.body(ErrorResponse.from(errorCode));
	}

	@ExceptionHandler(MethodArgumentNotValidException.class)
	public ResponseEntity<ErrorResponse> handleMethodArgumentNotValidException(
		MethodArgumentNotValidException e) {

		log.warn("요청 값 검증 실패: {}건의 오류 발생",
			e.getBindingResult().getErrorCount());

		List<ValidationFieldError> errors = e.getBindingResult()
			.getFieldErrors()
			.stream()
			.map(ValidationFieldError::from)
			.toList();

		return ResponseEntity
			.status(GlobalErrorCode.INVALID_REQUEST.getHttpStatus())
			.body(ErrorResponse.of(GlobalErrorCode.INVALID_REQUEST, errors));
	}

	@ExceptionHandler(DataIntegrityViolationException.class)
	public ResponseEntity<ErrorResponse> handleDataIntegrityViolationException(
		DataIntegrityViolationException e) {

		log.warn("DB 무결성 제약 위반 발생");
		log.debug("DB 무결성 제약 위반 상세 원인: {}", e.getMostSpecificCause().getMessage());

		return ResponseEntity
			.status(GlobalErrorCode.DATA_INTEGRITY_VIOLATION.getHttpStatus())
			.body(ErrorResponse.from(GlobalErrorCode.DATA_INTEGRITY_VIOLATION));
	}

	@ExceptionHandler(Exception.class)
	public ResponseEntity<ErrorResponse> handleException(Exception e) {
		log.error("처리되지 않은 예외 발생", e);
		return ResponseEntity
			.status(GlobalErrorCode.INTERNAL_SERVER_ERROR.getHttpStatus())
			.body(ErrorResponse.from(GlobalErrorCode.INTERNAL_SERVER_ERROR));
	}
}