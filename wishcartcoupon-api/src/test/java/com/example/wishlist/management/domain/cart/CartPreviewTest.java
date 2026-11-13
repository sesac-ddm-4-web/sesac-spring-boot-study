package com.example.wishlist.management.domain.cart;

import com.example.wishlist.management.domain.wish.Wish;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CartPreviewTest {
    // 단가 × 수량으로 원하는 소계를 가진 장바구니를 만든다
    private Cart cart(int price, int quantity) {
        Cart cart = new Cart();
        cart.add(new Wish(1L, "상품", price), quantity);
        return cart;
    }

    @Test
    void percent10_at110000() {
        CartPreview p = cart(22_000, 5).preview(CouponType.PERCENT10);   // 소계 110,000
        assertEquals(110_000, p.subtotal());
        assertEquals(11_000, p.discount());
        assertEquals(99_000, p.discountedSubtotal());
        assertEquals(5_000, p.shipping());        // 할인 후 10만 원 미만 → 배송비 부과
        assertEquals(104_000, p.finalAmount());
    }

    @Test void fixed15000_at110000() {
        CartPreview p = cart(22_000, 5).preview(CouponType.FIXED15000);
        assertEquals(15_000, p.discount());
        assertEquals(100_000, p.finalAmount());
    }

    @Test void percent10_isCappedAt20000() {
        CartPreview p = cart(60_000, 5).preview(CouponType.PERCENT10);   // 소계 300,000
        assertEquals(20_000, p.discount());       // 30,000이 아니라 상한 20,000
        assertEquals(0, p.shipping());
        assertEquals(280_000, p.finalAmount());
    }

    @Test void percent10_dropsFractionBelowWon() {
        CartPreview p = cart(12_345, 1).preview(CouponType.PERCENT10);
        assertEquals(1_234, p.discount());        // 1,234.5 → 버림
    }

    @Test void shippingBoundary() {
        assertEquals(0, cart(20_000, 5).preview(CouponType.NONE).shipping());       // 정확히 100,000
        assertEquals(5_000, cart(99_999, 1).preview(CouponType.NONE).shipping());   // 99,999
    }

    @Test void fixed15000_rejectedBelow100000() {
        assertThrows(IllegalArgumentException.class,
                () -> cart(99_999, 1).preview(CouponType.FIXED15000));
    }

    @Test void emptyCart_allZero_andOnlyNone() {
        CartPreview p = new Cart().preview(CouponType.NONE);
        assertEquals(0, p.finalAmount());
        assertThrows(IllegalArgumentException.class, () -> new Cart().preview(CouponType.PERCENT10));
    }
}