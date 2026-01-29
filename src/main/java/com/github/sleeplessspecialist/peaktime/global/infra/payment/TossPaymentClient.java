package com.github.sleeplessspecialist.peaktime.global.infra.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossErrorDto;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 토스 페이먼츠(Toss Payments) 외부 API와의 HTTP 통신을 전담하는 클라이언트 클래스입니다.
 * <p>
 * 결제 승인 요청(Confirm) 전송, 인증 헤더(Basic Auth) 설정, 그리고 응답 에러 핸들링을 수행합니다.
 * </p>
 *
 * @author 기섭
 * @version 1.0
 * @since 2026. 1. 26.
 */
@Slf4j
@Component
public class TossPaymentClient {

	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	// 생성자 주입
	public TossPaymentClient(
		RestClient.Builder builder,
		ObjectMapper objectMapper,
		@Value("${payment.toss.url}") String baseUrl,
		@Value("${payment.toss.secret-key}") String secretKey
	) {
		this.objectMapper = objectMapper;

		// 생성자 내부에서 암호화 및 RestClient 초기화 수행
		String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		this.restClient = builder
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	/**
	 * 토스 페이먼츠에 결제 승인을 요청합니다.
	 * 실패 시 4xx, 5xx 에러 바디를 파싱하여 로그를 남깁니다.
	 *
	 * @param req 결제 승인 요청 정보 (paymentKey, orderId, amount)
	 * @return 결제 승인 성공 응답
	 */
	public TossPaymentConfirmRes confirm(TossPaymentConfirmReq req) {
		return restClient.post()
			.uri("/confirm")
			.body(req)
			.retrieve()
			.onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
				// 1. 에러 바디 읽기
				String errorBodyStr = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

				// 2. 로그 상세 기록
				log.error("토스 결제 실패 응답: Status={}, Body={}", response.getStatusCode(), errorBodyStr);

				// 3. 에러 메시지 파싱
				try {
					TossErrorDto errorDto = objectMapper.readValue(errorBodyStr, TossErrorDto.class);
					throw new RuntimeException("토스 결제 실패: " + errorDto.getMessage() + " (" + errorDto.getCode() + ")");

				} catch (Exception e) {
					throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
				}
			})
			.body(TossPaymentConfirmRes.class);
	}


	/**
	 * 토스 페이먼츠에 결제 취소를 요청
	 * <p>
	 * paymentKey 기준으로 취소 요청을 전송하며, cancelReason 은 필수.
	 * </p>
	 *
	 * @param paymentKey 토스 결제 키
	 * @param req 취소 요청 바디 (cancelReason 등)
	 * @return 결제 취소 성공 응답
	 */
	public TossPaymentCancelRes cancel(String paymentKey, TossPaymentCancelReq req) {
		return restClient.post()
			.uri("/{paymentKey}/cancel", paymentKey)
			.body(req)
			.retrieve()
			.onStatus(status -> status.is4xxClientError() || status.is5xxServerError(), (request, response) -> {
				// 1. 에러 바디 읽기
				String errorBodyStr = new String(response.getBody().readAllBytes(), StandardCharsets.UTF_8);

				// 2. 로그 상세 기록
				log.error("토스 결제 취소 실패 응답: Status={}, Body={}", response.getStatusCode(), errorBodyStr);

				// 3. 에러 메시지 파싱
				try {
					TossErrorDto errorDto = objectMapper.readValue(errorBodyStr, TossErrorDto.class);
					throw new RuntimeException("토스 결제 실패: " + errorDto.getMessage() + " (" + errorDto.getCode() + ")");

				} catch (Exception e) {
					throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
				}
			})
			.body(TossPaymentCancelRes.class);
	}
}