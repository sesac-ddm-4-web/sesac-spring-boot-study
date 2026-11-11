package com.example.demo.cart.domain;

import com.example.demo.global.exception.ConflictException;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.item.domain.CollectionItem;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private static final int MAX_KINDS = 20;

    private final List<CartItem> items = new ArrayList<>();
    private long nextId = 1L;

    public void add(CollectionItem item, int quantity) {
        CartItem existing = items.stream()
                .filter(cartItem -> cartItem.isSameItem(item.getId()))
                .findFirst()
                .orElse(null);

        if (existing != null) {
            existing.addQuantity(quantity);
            return;
        }

        if (items.size() >= MAX_KINDS) {
            throw new ConflictException("장바구니에는 최대 " + MAX_KINDS + "종까지 담을 수 있습니다.");
        }

        CartItem newItem = CartItem.create(nextId, item, quantity);
        items.add(newItem);
        nextId++;
    }

    public void changeQuantity(Long cartItemId, int quantity) {
        findItem(cartItemId).changeQuantity(quantity);
    }

    public void remove(Long cartItemId) {
        items.remove(findItem(cartItemId));
    }

    public long subtotal() {
        return items.stream()
                .mapToLong(CartItem::lineTotal)
                .sum();
    }

    public PricePreview preview(Coupon coupon) {
        return PricePreview.of(subtotal(), coupon);
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    private CartItem findItem(Long cartItemId) {
        return items.stream()
                .filter(cartItem -> cartItem.hasId(cartItemId))
                .findFirst()
                .orElseThrow(() -> new EntityNotFoundException("장바구니 항목을 찾지 못했습니다."));
    }
}
