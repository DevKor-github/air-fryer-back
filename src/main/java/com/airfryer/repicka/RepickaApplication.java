package com.airfryer.repicka;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableJpaAuditing
public class RepickaApplication {

	public static void main(String[] args) {
		SpringApplication.run(RepickaApplication.class, args);
	}

}
