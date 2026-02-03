package com.github.sleeplessspecialist.peaktime.global.config.async;

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * 비동기 작업 처리를 위한 Async 설정 클래스.
 * <p>
 * 결제 후처리 등 비동기 이벤트 리스너에서 사용할 전용 스레드 풀 Executor를 등록한다.
 * 비밀번호 초기화/이메일 인증 등 SMTP 메일 발송 작업도 별도 Executor로 분리하여 처리한다.
 * </p>
 *
 * @author 주우재, 재원
 * @version 1.1
 * @since 2026.01.30
 */
@EnableAsync
@Configuration
public class AsyncConfig {

	@Bean(name = "paymentAsyncExecutor")
	public Executor paymentAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(8);
		executor.setMaxPoolSize(16);
		executor.setQueueCapacity(500);
		executor.setThreadNamePrefix("payment-async-");
		executor.initialize();
		return executor;
	}

	/**
	 * SMTP 메일 발송 전용 비동기 Executor.
	 *
	 * <p>
	 * 비밀번호 초기화, 이메일 인증 등 외부 SMTP 서버(Gmail)와 통신하는 작업은
	 * 네트워크 지연 및 실패 가능성이 높아 별도의 스레드 풀로 분리하여 처리한다.
	 * </p>
	 *
	 * <p>
	 * 결제 후처리 Executor와 분리함으로써,
	 * 메일 발송 지연이 다른 비즈니스 비동기 작업에 영향을 주지 않도록 한다.
	 * </p>
	 *
	 * @return SMTP 메일 발송용 ThreadPoolTaskExecutor
	 */
	@Bean(name = "mailAsyncExecutor")
	public Executor mailAsyncExecutor() {
		ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
		executor.setCorePoolSize(4);
		executor.setMaxPoolSize(8);
		executor.setQueueCapacity(300);
		executor.setThreadNamePrefix("mail-async-");
		executor.initialize();
		return executor;
	}
}
