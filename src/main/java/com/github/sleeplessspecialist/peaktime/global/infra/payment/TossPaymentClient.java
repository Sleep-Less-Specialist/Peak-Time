package com.github.sleeplessspecialist.peaktime.global.infra.payment;

import java.nio.charset.StandardCharsets;
import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossErrorDto;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentCancelRes;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmReq;
import com.github.sleeplessspecialist.peaktime.domain.payment.dto.TossPaymentConfirmRes;
import com.github.sleeplessspecialist.peaktime.global.common.error.CustomException;
import com.github.sleeplessspecialist.peaktime.global.common.error.GlobalErrorCode;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Component
public class TossPaymentClient {

	private final ObjectMapper objectMapper;
	private final RestClient restClient;

	public TossPaymentClient(
		RestClient.Builder builder,
		ObjectMapper objectMapper,
		@Value("${payment.toss.url}") String baseUrl,
		@Value("${payment.toss.secret-key}") String secretKey
	) {
		this.objectMapper = objectMapper;

		String encodedKey = Base64.getEncoder().encodeToString((secretKey + ":").getBytes(StandardCharsets.UTF_8));

		this.restClient = builder
			.baseUrl(baseUrl)
			.defaultHeader(HttpHeaders.AUTHORIZATION, "Basic " + encodedKey)
			.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
			.build();
	}

	public TossPaymentConfirmRes confirm(TossPaymentConfirmReq req) {
		try {
			return restClient.post()
				.uri("/confirm")
				.body(req)
				.retrieve()
				.body(TossPaymentConfirmRes.class);
		} catch (RestClientResponseException e) {
			handleTossError("confirm", e);
			throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	public TossPaymentCancelRes cancel(String paymentKey, TossPaymentCancelReq req) {
		try {
			return restClient.post()
				.uri("/{paymentKey}/cancel", paymentKey)
				.body(req)
				.retrieve()
				.body(TossPaymentCancelRes.class);
		} catch (RestClientResponseException e) {
			handleTossError("cancel", e);
			throw new CustomException(GlobalErrorCode.INTERNAL_SERVER_ERROR);
		}
	}

	private void handleTossError(String operation, RestClientResponseException e) {
		String errorBody = e.getResponseBodyAsString(StandardCharsets.UTF_8);
		try {
			TossErrorDto errorDto = objectMapper.readValue(errorBody, TossErrorDto.class);
			log.error("Toss {} failed. status={}, code={}, message={}",
				operation,
				e.getStatusCode(),
				errorDto.getCode(),
				errorDto.getMessage());
			return;
		} catch (Exception ignored) {
			log.error("Toss {} failed. status={}, rawBody={}", operation, e.getStatusCode(), errorBody);
		}
	}
}
