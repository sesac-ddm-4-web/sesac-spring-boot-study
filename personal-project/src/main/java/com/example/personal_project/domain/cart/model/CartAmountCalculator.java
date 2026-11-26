package com.example.personal_project.domain.cart.model;

import com.example.personal_project.global.exception.InvalidCouponException;
import com.example.personal_project.global.exception.CartAmountException;
import java.util.List;

public class CartAmountCalculator {
    public CartPreview bestCoupon(List<CartItem> items) {
        CartPreview best = calculate(items, Coupon.NONE);
        // Strict comparison preserves NONE → PERCENT10 → FIXED15000 tie priority.
        for (Coupon coupon : List.of(Coupon.PERCENT10, Coupon.FIXED15000)) {
            try {
                CartPreview candidate = calculate(items, coupon);
                if (candidate.finalAmount() < best.finalAmount()) best = candidate;
            } catch (InvalidCouponException ignored) {
                // Unavailable coupons do not participate in the comparison.
            }
        }
        return best;
    }

    public CartPreview calculate(List<CartItem> items, Coupon coupon) {
        if (coupon == null) throw new InvalidCouponException("쿠폰이 필요합니다.");
        if (items.isEmpty()) {
            if (coupon != Coupon.NONE) {
                throw new InvalidCouponException("빈 장바구니에는 NONE만 사용할 수 있습니다.");
            }
            return new CartPreview(0, 0, 0, 0, 0, coupon);
        }
        try {
            long subtotal = 0L;
            for (CartItem item : items) {
                if (item.unitPrice() < 0) {
                    throw new IllegalArgumentException("상품 가격은 0 이상이어야 합니다.");
                }
                subtotal = Math.addExact(subtotal,
                        Math.multiplyExact(item.unitPrice(), (long) item.quantity()));
            }
            long discount = switch (coupon) {
                case NONE -> 0L;
                case PERCENT10 -> Math.min(subtotal / 10, 20_000L);
                case FIXED15000 -> {
                    if (subtotal < 100_000L) {
                        throw new InvalidCouponException("FIXED15000은 상품 합계 100,000원 이상부터 사용할 수 있습니다.");
                    }
                    yield 15_000L;
                }
            };
            long discountedSubtotal = subtotal - discount;
            long shipping = discountedSubtotal >= 100_000L ? 0L : 5_000L;
            return new CartPreview(subtotal, discount, discountedSubtotal, shipping,
                    Math.addExact(discountedSubtotal, shipping), coupon);
        } catch (ArithmeticException exception) {
            throw new CartAmountException("장바구니 금액이 계산 가능한 범위를 초과했습니다.", exception);
        }
    }
}
