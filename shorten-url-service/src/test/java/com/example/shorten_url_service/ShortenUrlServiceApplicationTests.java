package com.example.shorten_url_service;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest
@ActiveProfiles("test")
class ShortenUrlServiceApplicationTests {

	@Test
	void connectsToMySql(@Autowired JdbcTemplate jdbcTemplate) {
		String database = jdbcTemplate.queryForObject("SELECT DATABASE()", String.class);
		assertEquals("shorten_url_service_test", database);
	}

}
