package com.example.sesac_spring_boot_study.domain;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartTest {
    private Cart cart;

    @BeforeEach
    void setUp() { cart = new Cart(); }

    @Test
    @DisplayName("같은 상품은 수량 합산")
    void merge() {
        cart.add(1L, "펜", 5000L, null, 2);
        cart.add(1L, "펜", 5000L, null, 2);

        assertEquals(1, cart.getItems().size());
        assertEquals(4, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("합산 5 초과는 예외, 수량 유지")
    void overFive() {
        cart.add(1L, "펜", 5000L, null, 4);

        assertThrows(IllegalStateException.class,
                () -> cart.add(1L, "펜", 5000L, null, 2));
        assertEquals(4, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("21번째 종류는 거부, 20개 유지")
    void maxKinds() {
        for (long i = 1; i <= 20; i++) {
            cart.add(i, "상품" + i, 1000L, null, 1);
        }
        assertThrows(IllegalStateException.class,
                () -> cart.add(21L, "상품21", 1000L, null, 1));
        assertEquals(20, cart.getItems().size());
    }

    @Test
    @DisplayName("subtotal은 단가×수량의 합")
    void subtotal() {
        cart.add(1L, "펜", 5000L, null, 2);
        cart.add(2L, "폰", 9990L, 3, 3);

        assertEquals(39_970L, cart.subtotal());
    }

    @Test
    @DisplayName("subtotal 110,000 쿠폰별 금액")
    void previewByCoupon() {
        cart.add(1L, "모니터", 22000, null, 5);

        CartPreview none = cart.preview(Coupon.NONE);
        assertEquals(110_000, none.finalAmount());
        assertEquals(0, none.shipping());

        CartPreview percent = cart.preview(Coupon.PERCENT10);
        assertEquals(11_000, percent.discount());
        assertEquals(5_000, percent.shipping());
        assertEquals(104_000, percent.finalAmount());

        CartPreview fixed = cart.preview(Coupon.FIXED15000);
        assertEquals(15_000, fixed.discount());
        assertEquals(100_000, fixed.finalAmount());
    }

    @Test
    @DisplayName("빈 장바구니는 모두 0, NONE 외 쿠폰은 거부")
    void previewEmpty() {
        assertEquals(0, cart.preview(Coupon.NONE).finalAmount());
        assertThrows(IllegalArgumentException.class, () -> cart.preview(Coupon.PERCENT10));
    }

    @Test
    @DisplayName("미리보기는 장바구니를 바꾸지 않는다")
    void previewDoesNotChange() {
        cart.add(1L, "펜", 5000, null, 2);
        cart.preview(Coupon.NONE);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQuantity());
    }

    @Test
    @DisplayName("모르는 쿠폰 코드는 400 대상 예외")
    void unknownCoupon() {
        assertThrows(IllegalArgumentException.class, () -> Coupon.from("ABC"));
    }
}