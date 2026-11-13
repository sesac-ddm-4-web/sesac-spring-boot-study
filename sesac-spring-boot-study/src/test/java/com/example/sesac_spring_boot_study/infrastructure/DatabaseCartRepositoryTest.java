package com.example.sesac_spring_boot_study.infrastructure;

import com.example.sesac_spring_boot_study.domain.CartItem;
import com.example.sesac_spring_boot_study.domain.CartRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.dao.DuplicateKeyException;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class DatabaseCartRepositoryTest {

    @Autowired
    private CartRepository cartRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
    }

    @Test
    @DisplayName("add: row_id가 부여되고 복사본(이름·단가·재고)이 저장된다")
    void add() {
        CartItem saved = cartRepository.add(new CartItem(null, 10L, "펜", 5000L, 3, 2));

        assertNotNull(saved.getRowId());
        CartItem found = cartRepository.findAll().get(0);
        assertEquals(10L, found.getProductId());
        assertEquals("펜", found.getName());
        assertEquals(5000L, found.getPrice());
        assertEquals(3, found.getStock());
        assertEquals(2, found.getQuantity());
    }

    @Test
    @DisplayName("수동 상품의 stock은 NULL로 저장·복원된다")
    void nullStock() {
        cartRepository.add(new CartItem(null, 10L, "펜", 5000L, null, 1));

        assertNull(cartRepository.findAll().get(0).getStock());
    }

    @Test
    @DisplayName("findAll은 담은 순서(row_id 순)")
    void findAllOrder() {
        cartRepository.add(new CartItem(null, 3L, "C", 300L, null, 1));
        cartRepository.add(new CartItem(null, 1L, "A", 100L, null, 1));

        List<CartItem> all = cartRepository.findAll();

        assertEquals(List.of("C", "A"), all.stream().map(CartItem::getName).toList());
    }

    @Test
    @DisplayName("update: 수량만 바뀌고 복사본은 유지된다")
    void updateQuantityOnly() {
        CartItem saved = cartRepository.add(new CartItem(null, 10L, "펜", 5000L, 3, 2));
        saved.setQuantity(4);

        cartRepository.update(saved);

        CartItem found = cartRepository.findAll().get(0);
        assertEquals(4, found.getQuantity());
        assertEquals("펜", found.getName());
        assertEquals(5000L, found.getPrice());
    }

    @Test
    @DisplayName("delete: 행이 삭제된다")
    void delete() {
        CartItem saved = cartRepository.add(new CartItem(null, 10L, "펜", 5000L, null, 1));

        cartRepository.delete(saved.getRowId());

        assertTrue(cartRepository.findAll().isEmpty());
    }

    @Test
    @DisplayName("같은 product_id는 UNIQUE 제약으로 거부")
    void uniqueProduct() {
        cartRepository.add(new CartItem(null, 10L, "펜", 5000L, null, 1));

        assertThrows(DuplicateKeyException.class, () ->
                cartRepository.add(new CartItem(null, 10L, "펜", 5000L, null, 1)));
        assertEquals(1, cartRepository.findAll().size());
    }
}