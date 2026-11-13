package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.domain.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseProductRepositoryTest {

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE products");   // id도 1부터 다시 시작
    }

    @Test
    @DisplayName("수동 상품 저장: id 부여, 가격 0 허용, 외부 필드는 null")
    void addManual() {
        Product saved = productRepository.add(new Product(null, "무료 샘플", 0));

        assertEquals(1L, saved.getId());
        Product found = productRepository.findById(saved.getId());
        assertEquals("무료 샘플", found.getName());
        assertEquals(0, found.getPrice());
        assertEquals(SourceType.MANUAL, found.getSourceType());
        assertNull(found.getExternalId());
        assertNull(found.getStock());
    }

    @Test
    @DisplayName("외부 상품 저장: source·externalId·stock을 그대로 복원")
    void addExternal() {
        Product saved = productRepository.add(
                new Product(null, "폰", 9990, SourceType.EXTERNAL, 7L, 0));   // 품절도 저장

        Product found = productRepository.findById(saved.getId());
        assertEquals(SourceType.EXTERNAL, found.getSourceType());
        assertEquals(7L, found.getExternalId());
        assertEquals(0, found.getStock());
        assertNotEquals(found.getId(), found.getExternalId());   // 로컬 id와 외부 id 구분
    }

    @Test
    @DisplayName("없는 id 조회는 NoSuchElementException")
    void findByIdNotFound() {
        assertThrows(NoSuchElementException.class, () -> productRepository.findById(999L));
    }

    @Test
    @DisplayName("findAll은 등록 순서(id 순)를 유지")
    void findAllOrder() {
        productRepository.add(new Product(null, "C", 300));
        productRepository.add(new Product(null, "A", 100));
        productRepository.add(new Product(null, "B", 200));

        List<Product> all = productRepository.findAll();

        assertEquals(List.of("C", "A", "B"), all.stream().map(Product::getName).toList());
    }

    @Test
    @DisplayName("수동 상품은 이름이 같아도 저장 가능")
    void duplicateNameAllowed() {
        productRepository.add(new Product(null, "펜", 1000));
        productRepository.add(new Product(null, "펜", 1000));

        assertEquals(2, productRepository.findAll().size());
    }

    @Test
    @DisplayName("existsByExternalId: 저장 전 false, 저장 후 true")
    void existsByExternalId() {
        assertFalse(productRepository.existsByExternalId(SourceType.EXTERNAL, 1L));

        productRepository.add(new Product(null, "폰", 9990, SourceType.EXTERNAL, 1L, 10));

        assertTrue(productRepository.existsByExternalId(SourceType.EXTERNAL, 1L));
        assertFalse(productRepository.existsByExternalId(SourceType.EXTERNAL, 2L));
    }

    @Test
    @DisplayName("같은 외부 식별자는 DB UNIQUE 제약으로 거부, 기존 목록 유지")
    void uniqueConstraint() {
        productRepository.add(new Product(null, "폰", 9990, SourceType.EXTERNAL, 1L, 10));

        assertThrows(DuplicateKeyException.class, () ->
                productRepository.add(new Product(null, "폰 복사", 1, SourceType.EXTERNAL, 1L, 5)));

        assertEquals(1, productRepository.findAll().size());
    }
}