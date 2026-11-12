package com.example.personal_project.domain.cart.model;

import com.example.personal_project.domain.item.model.CollectionItem;
import com.example.personal_project.global.exception.CartConflictException;
import com.example.personal_project.global.exception.CartItemNotFoundException;

import java.util.ArrayList;
import java.util.List;

public class Cart {

    private final List<CartItem> items;
    private long nextItemId;

    public Cart() {
        this.items = new ArrayList<>();
        this.nextItemId = 1L;
    }

    private Cart(List<CartItem> items, long nextItemId) {
        this.items = new ArrayList<>(items);
        this.nextItemId = nextItemId;
    }

    public Cart copy() {
        return new Cart(items, nextItemId);
    }

    public List<CartItem> findAll() {
        return List.copyOf(items);
    }

    public void add(CollectionItem item, Integer quantity) {
        if (quantity < 1 || quantity > 5) {
            throw  new IllegalArgumentException("추가 수량은 1~5개여야 합니다");
        }

        for (int index = 0; index < items.size(); index++) {
            CartItem existing = items.get(index);

            if (existing.itemId().equals(item.id())) {
                CartItem changed = existing.withQuantity(existing.quantity() + quantity);

                items.set(index, changed);
                return;
            }
        }

        if (items.size() >= 20) {
            throw new CartConflictException("장바구니에는 최대 20종까지 담을 수 있습니다.");
        }

        CartItem newItem = new CartItem(
                nextItemId,
                item.id(),
                item.name(),
                item.price(),
                item.sourceType(),
                item.stock(),
                quantity
        );

        items.add(newItem);
        nextItemId++;
    }
    public void changeQuantity(Long cartItemId, int quantity) {
        int index = findIndex(cartItemId);
        CartItem changed = items.get(index).withQuantity(quantity);
        items.set(index, changed);
    }

    public void remove(Long cartItemId) {
        items.remove(findIndex(cartItemId));
    }

    private int findIndex(Long cartItemId) {
        for (int index = 0; index < items.size(); index++) {
            if (items.get(index).cartItemId().equals(cartItemId)) {
                return index;
            }
        }
        throw new CartItemNotFoundException(cartItemId);
    }
}
