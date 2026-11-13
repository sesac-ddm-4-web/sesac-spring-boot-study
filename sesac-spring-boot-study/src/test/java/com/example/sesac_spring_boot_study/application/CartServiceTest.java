package com.example.sesac_spring_boot_study.application;

import com.example.sesac_spring_boot_study.domain.CartItem;
import com.example.sesac_spring_boot_study.domain.CartPreview;
import com.example.sesac_spring_boot_study.domain.Product;
import com.example.sesac_spring_boot_study.domain.ProductRepository;
import com.example.sesac_spring_boot_study.domain.SourceType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.List;
import java.util.NoSuchElementException;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class CartServiceDbTest {

    @Autowired
    private CartService cartService;

    @Autowired
    private ProductRepository productRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Long manualId;     // 재고 제한 없는 수동 상품 (22,000원)
    private Long externalId;   // 재고 3인 외부 상품 (9,990원)
    private Long soldOutId;    // 품절 외부 상품

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE products");
        manualId = productRepository.add(new Product(null, "모니터", 22000)).getId();
        externalId = productRepository.add(
                new Product(null, "폰", 9990, SourceType.EXTERNAL, 1L, 3)).getId();
        soldOutId = productRepository.add(
                new Product(null, "품절폰", 1000, SourceType.EXTERNAL, 2L, 0)).getId();
    }

    @Test
    @DisplayName("담기: 새 행은 저장되고, 같은 상품은 수량이 합산된다")
    void addAndMerge() {
        cartService.addItem(manualId, 2);
        CartItem merged = cartService.addItem(manualId, 2);

        List<CartItem> items = cartService.getItems();
        assertEquals(1, items.size());
        assertEquals(4, items.get(0).getQuantity());
        assertEquals(merged.getRowId(), items.get(0).getRowId());
    }

    @Test
    @DisplayName("합산 5 초과는 예외이고 DB 수량은 유지")
    void overFiveKeepsDb() {
        cartService.addItem(manualId, 4);

        assertThrows(IllegalStateException.class, () -> cartService.addItem(manualId, 2));

        assertEquals(4, cartService.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("수량 0, 6은 IllegalArgumentException이고 저장되지 않는다")
    void rangeCheck() {
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(manualId, 0));
        assertThrows(IllegalArgumentException.class, () -> cartService.addItem(manualId, 6));

        assertTrue(cartService.getItems().isEmpty());
    }

    @Test
    @DisplayName("외부 상품 재고 초과·품절은 예외이고 저장되지 않는다")
    void stockRules() {
        assertThrows(IllegalStateException.class, () -> cartService.addItem(externalId, 4));
        assertThrows(IllegalStateException.class, () -> cartService.addItem(soldOutId, 1));

        assertTrue(cartService.getItems().isEmpty());
    }

    @Test
    @DisplayName("없는 상품은 NoSuchElementException")
    void unknownProduct() {
        assertThrows(NoSuchElementException.class, () -> cartService.addItem(9999L, 1));
    }

    @Test
    @DisplayName("수량 변경은 교체이며 실패 시 기존 수량 유지")
    void changeQuantity() {
        CartItem item = cartService.addItem(externalId, 2);

        cartService.changeQuantity(item.getRowId(), 3);
        assertEquals(3, cartService.getItems().get(0).getQuantity());

        assertThrows(IllegalStateException.class,
                () -> cartService.changeQuantity(item.getRowId(), 4));   // 재고 3 초과
        assertEquals(3, cartService.getItems().get(0).getQuantity());

        assertThrows(NoSuchElementException.class, () -> cartService.changeQuantity(999L, 1));
    }

    @Test
    @DisplayName("삭제 후 다시 담으면 현재 상품 값을 새로 복사한다")
    void removeAndReAdd() {
        CartItem item = cartService.addItem(manualId, 1);
        cartService.remove(item.getRowId());
        assertTrue(cartService.getItems().isEmpty());

        jdbcTemplate.update("UPDATE products SET price = 30000 WHERE id = ?", manualId);
        cartService.addItem(manualId, 1);

        assertEquals(30000L, cartService.getItems().get(0).getPrice());
        assertThrows(NoSuchElementException.class, () -> cartService.remove(999L));
    }

    @Test
    @DisplayName("처음 복사한 값은 원본이 바뀌거나 삭제돼도 유지된다")
    void copyKept() {
        cartService.addItem(manualId, 1);

        jdbcTemplate.update("UPDATE products SET price = 1, name = '변경' WHERE id = ?", manualId);
        cartService.addItem(manualId, 1);   // 합산: 처음 값 유지
        jdbcTemplate.update("DELETE FROM products WHERE id = ?", manualId);

        CartItem item = cartService.getItems().get(0);
        assertEquals("모니터", item.getName());
        assertEquals(22000L, item.getPrice());
        assertEquals(2, item.getQuantity());
    }

    @Test
    @DisplayName("20종 초과는 예외, 이미 담은 상품 합산은 가능")
    void maxKinds() {
        for (int i = 0; i < 19; i++) {   // 모니터·폰 제외하고 채우기 위해 새 상품 19개 + 모니터 = 20종
            Long id = productRepository.add(new Product(null, "상품" + i, 1000)).getId();
            cartService.addItem(id, 1);
        }
        cartService.addItem(manualId, 1);   // 20종째

        assertThrows(IllegalStateException.class, () -> cartService.addItem(externalId, 1));
        cartService.addItem(manualId, 1);   // 기존 상품 합산은 OK
        assertEquals(20, cartService.getItems().size());
    }

    @Test
    @DisplayName("미리보기: 110,000원 기준 쿠폰별 금액, 저장은 변하지 않는다")
    void preview() {
        cartService.addItem(manualId, 5);   // 22,000 × 5 = 110,000
        List<CartItem> before = cartService.getItems();

        CartPreview none = cartService.preview(null);
        assertEquals(110_000, none.finalAmount());
        assertEquals(0, none.shipping());

        CartPreview percent = cartService.preview("PERCENT10");
        assertEquals(11_000, percent.discount());
        assertEquals(5_000, percent.shipping());
        assertEquals(104_000, percent.finalAmount());

        CartPreview fixed = cartService.preview("FIXED15000");
        assertEquals(15_000, fixed.discount());
        assertEquals(100_000, fixed.finalAmount());

        assertThrows(IllegalArgumentException.class, () -> cartService.preview("ABC"));
        assertEquals(before.size(), cartService.getItems().size());
        assertEquals(5, cartService.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("빈 장바구니: 모두 0, NONE 외 쿠폰은 거부")
    void previewEmpty() {
        assertEquals(0, cartService.preview(null).finalAmount());
        assertThrows(IllegalArgumentException.class, () -> cartService.preview("PERCENT10"));
    }
}