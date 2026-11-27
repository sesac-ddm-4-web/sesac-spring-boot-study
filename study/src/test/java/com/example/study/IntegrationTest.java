package com.example.study;

import static java.lang.annotation.ElementType.TYPE;
import static java.lang.annotation.RetentionPolicy.RUNTIME;

import java.lang.annotation.Retention;
import java.lang.annotation.Target;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;

/**
 * 통합 테스트의 공통 설정. 설정이 같으면 Spring 컨텍스트를 하나만 만들어 재사용한다.
 * 컨텍스트가 여러 개면 같은 테스트 DB의 테이블을 서로 만들고 지우게 된다.
 */
@Target(TYPE)
@Retention(RUNTIME)
@SpringBootTest
@AutoConfigureMockMvc
@Import(DatabaseCleaner.class)
public @interface IntegrationTest {
}
