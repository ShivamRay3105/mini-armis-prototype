package com.sr.Event_Ingestion_Service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class EventIngestionServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(EventIngestionServiceApplication.class, args);
	}

}
