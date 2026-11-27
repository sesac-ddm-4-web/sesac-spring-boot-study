package com.example.shorten_url_service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class ShortenUrlServiceApplication {

	public static void main(String[] args) {
		SpringApplication.run(ShortenUrlServiceApplication.class, args);
	}

}
