package com.ddiring.backend_market;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.cloud.openfeign.EnableFeignClients;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@SpringBootApplication
@EnableFeignClients
@EnableJpaAuditing
@EnableScheduling
public class BackendMarketApplication {

	public static void main(String[] args) {
		SpringApplication.run(BackendMarketApplication.class, args);
	}

}
