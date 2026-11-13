package com.example.wishlist.management.domain.cart;

import com.example.wishlist.management.domain.wish.Wish;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class CouponRecommendationTest {
    private Cart cart(int price, int quantity) {
        Cart cart = new Cart();
        cart.add(new Wish(1L, "상품", price), quantity);
        return cart;
    }

    @Test
    void picksLowestFinalAmount_at110000() {
        assertEquals(CouponType.FIXED15000, cart(22_000, 5).recommendCoupon().recommended());
    }

    @Test void excludesUnusableCoupon_at50000() {
        CouponRecommendation r = cart(50_000, 1).recommendCoupon();
        assertEquals(CouponType.PERCENT10, r.recommended());
        assertEquals(2, r.candidates().size());              // FIXED15000은 후보에서 제외
    }

    @Test void tieBetweenPercentAndFixed_prefersPercent10() {
        // 소계 150,000: 둘 다 135,000원
        assertEquals(CouponType.PERCENT10, cart(30_000, 5).recommendCoupon().recommended());
    }

    @Test void tieWithNone_prefersNone() {
        // 가격 0원 상품: NONE과 PERCENT10 모두 5,000원(배송비)
        assertEquals(CouponType.NONE, cart(0, 1).recommendCoupon().recommended());
    }

    @Test void emptyCart_recommendsNoneOnly() {
        CouponRecommendation r = new Cart().recommendCoupon();
        assertEquals(CouponType.NONE, r.recommended());
        assertEquals(1, r.candidates().size());
    }

    @Test void doesNotChangeCart() {
        Cart cart = cart(22_000, 5);
        cart.recommendCoupon();
        assertEquals(110_000, cart.subtotal());
        assertEquals(5, cart.getItems().get(0).getQuantity());
    }

    @Test void couponOrderMatchesTieBreakRule() {
        assertArrayEquals(
                new CouponType[]{CouponType.NONE, CouponType.PERCENT10, CouponType.FIXED15000},
                CouponType.values());                          // 선언 순서가 곧 동률 우선순위
    }
}