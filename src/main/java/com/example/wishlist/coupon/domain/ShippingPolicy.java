package com.example.wishlist.coupon.domain;

public final class ShippingPolicy {

    public static final long FREE_SHIPPING_THRESHOLD = 100_000L;
    public static final long SHIPPING_FEE = 5_000L;

    private ShippingPolicy() {}

    public static long feeFor(long subtotal, long discountedSubtotal) {
        if (subtotal == 0) return 0L;

        return discountedSubtotal >= FREE_SHIPPING_THRESHOLD ? 0L : SHIPPING_FEE;
    }

}
