package com.github.sleeplessspecialist.peaktime;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@EnableJpaAuditing
@SpringBootApplication
public class PeaktimeApplication {

	public static void main(String[] args) {
		SpringApplication.run(PeaktimeApplication.class, args);
	}

}
