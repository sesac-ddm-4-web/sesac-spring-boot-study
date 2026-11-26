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
    public ApplicationRunner runner(DataSource datasource) {
        return args -> {
            try (Connection connection = datasource.getConnection()) {
                // 시작 시 연결을 확인하고 풀에 반환한다.
            }
        };
    }

}
