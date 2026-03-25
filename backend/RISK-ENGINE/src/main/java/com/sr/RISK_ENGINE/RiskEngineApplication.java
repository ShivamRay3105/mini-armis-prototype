package com.sr.RISK_ENGINE;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class RiskEngineApplication {

	public static void main(String[] args) {
		SpringApplication.run(RiskEngineApplication.class, args);
	}

}
