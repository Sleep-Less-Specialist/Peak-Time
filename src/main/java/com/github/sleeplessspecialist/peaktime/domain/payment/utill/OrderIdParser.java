package com.github.sleeplessspecialist.peaktime.domain.payment.utill;

/**
 * Toss 에서 orderId 를 서버 DB 의 orderId로 변환하는 유틸 클래스
 * <p>
 * Toss 의 orderId 의 정책상 Base64 최소 6글자 이상 인코딩 형식
 * ORDER_{DATE}_{orderId}_{RANDOM} 형식의 orderId 에서
 * 각 데이터를 추출하는 유틸 클래스
 * extractCreatedAt: Toss 에서 실 결제 시간 추출
 * extractOrderId: 실제 DB 에 저장된 orderId 추출
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.28
 */
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class OrderIdParser {

	private static final DateTimeFormatter FORMATTER =
		DateTimeFormatter.ofPattern("yyyyMMddHHmmss");

	public static LocalDateTime extractCreatedAt(String orderId) {
		String[] parts = orderId.split("_");
		return LocalDateTime.parse(parts[1], FORMATTER);
	}

	public static String extractOrderId(String orderId) {
		String[] parts = orderId.split("_");
		return parts[2];
	}
}

