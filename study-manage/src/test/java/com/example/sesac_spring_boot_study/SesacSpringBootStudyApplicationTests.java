package com.example.sesac_spring_boot_study;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

@SpringBootTest(properties = {
	"spring.datasource.url=jdbc:h2:mem:study_manage_test",
	"spring.datasource.username=sa",
	"spring.datasource.password=",
	"spring.jpa.hibernate.ddl-auto=create-drop"
})
class SesacSpringBootStudyApplicationTests {

	@Test
	void contextLoads() {
		System.out.println("Test");
	}

}
