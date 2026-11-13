package com.example.hobby_wishlist.domain.cart;

public enum ShippingPolicy {
    STANDARD {
        @Override
        public Long getFee(Long subtotal) {
            return subtotal >= 100_000L ? 0L : 5000L;
        }
    };

    public abstract Long getFee(Long subtotal);
}
