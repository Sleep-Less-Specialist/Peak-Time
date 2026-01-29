package com.github.sleeplessspecialist.peaktime.global.config.async;

/**
 * .
 * <p>
 *
 * </p>
 *
 * @author 주우재
 * @version 1.0
 * @since
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
