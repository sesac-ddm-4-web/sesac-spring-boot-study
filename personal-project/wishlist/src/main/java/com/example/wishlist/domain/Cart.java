package com.example.wishlist.domain;

import java.util.List;

public class Cart {
    public static final int MAX_KINDS = 20;

    private final List<CartItem> items;

    public Cart(List<CartItem> items) {
        this.items = items;
    }

    public void checkCanAddNewKind() {
        if (items.size() >= MAX_KINDS) {
            throw new CartConflictException("장바구니에는 최대 20종까지 담을 수 있습니다.");
        }
    }

    public List<CartItem> getItems() {
        return items;
    }

    public int getTotalQuantity() {
        int total = 0;
        for (CartItem item : items) {
            total += item.getQuantity();
        }
        return total;
    }

    public long getSubtotal() {
        long subtotal = 0;
        for (CartItem item : items) {
            subtotal += item.getLineTotal();
        }
        return subtotal;
    }

    public PaymentPreview preview(Coupon coupon) {
        return PaymentPreview.of(getSubtotal(), coupon);
    }
}
