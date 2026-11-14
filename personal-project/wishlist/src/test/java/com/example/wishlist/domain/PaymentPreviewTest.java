package com.example.wishlist.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PaymentPreviewTest {

    @Test
    @DisplayName("상품 합계 110,000원 표: NONE 110,000 / PERCENT10 104,000 / FIXED15000 100,000")
    void table() {
        PaymentPreview none = PaymentPreview.of(110_000, Coupon.NONE);
        assertEquals(0, none.discount());
        assertEquals(0, none.shipping());
        assertEquals(110_000, none.finalAmount());

        PaymentPreview percent = PaymentPreview.of(110_000, Coupon.PERCENT10);
        assertEquals(11_000, percent.discount());
        assertEquals(99_000, percent.discountedSubtotal());
        assertEquals(5_000, percent.shipping());
        assertEquals(104_000, percent.finalAmount());

        PaymentPreview fixed = PaymentPreview.of(110_000, Coupon.FIXED15000);
        assertEquals(15_000, fixed.discount());
        assertEquals(5_000, fixed.shipping());
        assertEquals(100_000, fixed.finalAmount());
    }

    @Test
    @DisplayName("PERCENT10: 원 미만 버림, 최대 20,000원")
    void percentRule() {
        assertEquals(999, Coupon.PERCENT10.discount(9_999));
        assertEquals(20_000, Coupon.PERCENT10.discount(200_000));
        assertEquals(20_000, Coupon.PERCENT10.discount(500_000));
    }

    @Test
    @DisplayName("FIXED15000: 100,000원 이상만 사용 가능, 미만은 거부")
    void fixedRule() {
        assertEquals(15_000, Coupon.FIXED15000.discount(100_000));
        assertThrows(IllegalArgumentException.class, () -> Coupon.FIXED15000.discount(99_999));
    }

    @Test
    @DisplayName("배송비 경계: 할인 후 100,000원이면 무료, 99,999원이면 5,000원")
    void shippingBoundary() {
        assertEquals(0, PaymentPreview.of(100_000, Coupon.NONE).shipping());
        assertEquals(5_000, PaymentPreview.of(99_999, Coupon.NONE).shipping());
    }

    @Test
    @DisplayName("빈 장바구니: 모든 금액 0, NONE만 사용 가능")
    void emptyCart() {
        PaymentPreview empty = PaymentPreview.of(0, Coupon.NONE);
        assertEquals(0, empty.subtotal());
        assertEquals(0, empty.discount());
        assertEquals(0, empty.shipping());
        assertEquals(0, empty.finalAmount());

        assertThrows(IllegalArgumentException.class, () -> PaymentPreview.of(0, Coupon.PERCENT10));
        assertThrows(IllegalArgumentException.class, () -> PaymentPreview.of(0, Coupon.FIXED15000));
    }

    @Test
    @DisplayName("쿠폰 코드: 생략하면 NONE, 모르는 코드는 거부")
    void couponCode() {
        assertEquals(Coupon.NONE, Coupon.from(null));
        assertEquals(Coupon.NONE, Coupon.from(""));
        assertEquals(Coupon.PERCENT10, Coupon.from("PERCENT10"));
        assertThrows(IllegalArgumentException.class, () -> Coupon.from("HALF50"));
    }
}
