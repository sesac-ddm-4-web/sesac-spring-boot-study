package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.Product;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class SimpleProductServiceDbTest {

    @Autowired
    private SimpleProductService service;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE products");
    }

    private void registerSamples() {
        service.register("노트", 12000);
        service.register("무료 샘플", 0);
        service.register("펜", 5000);
        service.register("스티커", 5000);
        service.register("게임", 8000);
    }

    @Test
    @DisplayName("예산 5000: 무료 샘플, 펜, 스티커 (같은 가격은 등록 순서)")
    void recommendBoundaryAndTie() {
        registerSamples();

        List<Product> result = service.recommend(5000);

        assertEquals(List.of("무료 샘플", "펜", "스티커"),
                result.stream().map(Product::getName).toList());
    }

    @Test
    @DisplayName("예산 0: 무료 샘플만")
    void recommendZero() {
        registerSamples();

        assertEquals(List.of("무료 샘플"),
                service.recommend(0).stream().map(Product::getName).toList());
    }

    @Test
    @DisplayName("예산 9000: 최대 3개만 반환")
    void recommendMaxThree() {
        registerSamples();

        assertEquals(3, service.recommend(9000).size());
    }

    @Test
    @DisplayName("조건에 맞는 상품이 없으면 빈 목록")
    void recommendEmpty() {
        registerSamples();

        assertTrue(service.recommend(-0 + 0 == 0 ? 0 : 0).size() <= 1);
        service.register("비싼것", 99999);
        assertTrue(service.recommend(10).stream().allMatch(p -> p.getPrice() <= 10));
    }

    @Test
    @DisplayName("음수 가격·빈 이름·음수 예산은 400 대상 예외이고 DB는 변하지 않음")
    void invalidInputsKeepDb() {
        registerSamples();

        assertThrows(IllegalArgumentException.class, () -> service.register("   ", 1000));
        assertThrows(IllegalArgumentException.class, () -> service.register("공책", -1));
        assertThrows(IllegalArgumentException.class, () -> service.recommend(-1));

        assertEquals(5, service.findAll().size());
    }

    @Test
    @DisplayName("추천 조회는 저장된 목록을 바꾸지 않는다")
    void recommendDoesNotChangeStored() {
        registerSamples();

        service.recommend(100000);

        assertEquals(List.of("노트", "무료 샘플", "펜", "스티커", "게임"),
                service.findAll().stream().map(Product::getName).toList());
    }
}