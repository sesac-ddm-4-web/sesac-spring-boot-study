
package com.example.campkit;

import org.modelmapper.ModelMapper;
import org.modelmapper.config.Configuration;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import javax.sql.DataSource;
import java.sql.Connection;

@SpringBootApplication
public class CampkitApplication {

	public static void main(String[] args) {
		SpringApplication.run(CampkitApplication.class, args);
	}

	// ModelMapper 빈 등록
	@Bean
	public ModelMapper modelMapper() {
		ModelMapper modelMapper = new ModelMapper();
		modelMapper.getConfiguration()
				.setFieldMatchingEnabled(true)
				.setFieldAccessLevel(Configuration.AccessLevel.PRIVATE);

		return modelMapper;
	}

	// MySQL 연결 확인
	@Bean
	public ApplicationRunner databaseConnectionTest(DataSource dataSource) {
		return args -> {
			try (Connection connection = dataSource.getConnection()) {
			}
		};
	}
}
