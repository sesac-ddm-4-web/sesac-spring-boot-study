package com.example.sesac_spring_boot_study;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class SesacSpringBootStudyApplication {

	public static void main(String[] args) {
		SpringApplication.run(SesacSpringBootStudyApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(DataSource dataSource){
		return args -> {
			Connection connection = dataSource.getConnection();
		};
	}
}
