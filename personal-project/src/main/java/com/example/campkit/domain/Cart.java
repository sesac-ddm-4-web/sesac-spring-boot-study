
package com.example.campkit.domain;

import com.example.campkit.dto.CartPreview;
import com.example.campkit.exception.ConflictException;
import com.example.campkit.exception.NotFoundException;

import java.util.List;

public class Cart {

    private static final int MAX_ITEM_TYPES = 20;
    private static final long FREE_SHIPPING_THRESHOLD = 100_000L;
    private static final long SHIPPING_FEE = 5_000L;

    private final List<CartItem> items;

    public Cart(List<CartItem> items) {
        this.items = List.copyOf(items);
    }

    // 장바구니에 상품 추가
    public CartItem add(CollectionItem source, int quantity) {

        CartItem existing = items.stream()
                .filter(item ->
                        item.getCollectionItemId() == source.getId())
                .findFirst()
                .orElse(null);

        if (existing != null) {
            return existing.addQuantity(quantity);
        }

        if (items.size() >= MAX_ITEM_TYPES) {
            throw new ConflictException(
                    "장바구니는 최대 20종까지 가능합니다."
            );
        }

        return new CartItem(
                0,
                source.getId(),
                source.getName(),
                source.getPrice(),
                source.getStock(),
                quantity
        );
    }

    // 수량 변경
    public CartItem update(long cartId, int quantity) {
        CartItem item = items.stream()
                .filter(i -> i.getId() == cartId)
                .findFirst()
                .orElseThrow(() ->
                        new NotFoundException(
                                "장바구니 항목을 찾을 수 없습니다."
                        )
                );

        return item.withQuantity(quantity);
    }

    // 쿠폰과 배송비를 반영한 최종 금액
    public CartPreview preview(Coupon coupon) {

        if (items.isEmpty()) {
            if (coupon != Coupon.NONE) {
                throw new IllegalArgumentException(
                        "빈 장바구니에는 쿠폰을 사용할 수 없습니다."
                );
            }

            return new CartPreview(0, 0, 0, 0, 0, coupon);
        }

        try {
            long subtotal = 0;

            for (CartItem item : items) {
                subtotal = Math.addExact(
                        subtotal, item.getLineAmount()
                );
            }

            long discount = coupon.calculateDiscount(subtotal);
            long discountedSubtotal = subtotal - discount;

            long shipping = discountedSubtotal >= FREE_SHIPPING_THRESHOLD
                    ? 0
                    : SHIPPING_FEE;

            long finalAmount = Math.addExact(
                    discountedSubtotal, shipping
            );

            return new CartPreview(
                    subtotal,
                    discount,
                    discountedSubtotal,
                    shipping,
                    finalAmount,
                    coupon
            );

        } catch (ArithmeticException e) {
            throw new IllegalArgumentException(
                    "계산 가능한 금액 범위를 초과했습니다."
            );
        }
    }
}
