package com.example.demo.cart.domain;

import com.example.demo.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class CouponTest {

    @Test
    void 쿠폰_코드를_생략하면_NONE이다() {
        assertEquals(Coupon.NONE, Coupon.from(null));
        assertEquals(Coupon.NONE, Coupon.from(""));
        assertEquals(Coupon.NONE, Coupon.from("   "));
    }

    @Test
    void 쿠폰_코드로_쿠폰을_찾는다() {
        assertEquals(Coupon.PERCENT10, Coupon.from("PERCENT10"));
        assertEquals(Coupon.FIXED15000, Coupon.from(" FIXED15000 "));
    }

    @Test
    void 알_수_없는_쿠폰_코드는_400_예외가_발생한다() {
        assertThrows(BadRequestException.class, () -> Coupon.from("ABC"));
        assertThrows(BadRequestException.class, () -> Coupon.from("percent10"));
    }
}
