package com.github.sleeplessspecialist.peaktime.global.config.async;
/**
 * 비동기 작업 처리를 위한 Async 설정 클래스.
 * <p>
 * 결제 후처리 등 비동기 이벤트 리스너에서 사용할
 * 전용 스레드 풀 Executor를 등록한다.
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since 2026.01.30
 */

import java.util.concurrent.Executor;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

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
}
