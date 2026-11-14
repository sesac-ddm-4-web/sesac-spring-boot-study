package com.example.personal_project;

import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class PersonalProjectApplication {
	public static void main(String[] args) {
		SpringApplication.run(PersonalProjectApplication.class, args);
	}

	@Bean
	public ApplicationRunner runner(DataSource dataSource){
		return args -> {
			Connection connection = dataSource.getConnection();
		};
	}
}
