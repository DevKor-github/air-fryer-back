package com.airfryer.repicka;

import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableJpaAuditing			// Auditing 사용
@EnableBatchProcessing      // Spring Batch 사용
@EnableScheduling           // Spring Scheduler 사용
public class RepickaApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepickaApplication.class, args);
	}

}
