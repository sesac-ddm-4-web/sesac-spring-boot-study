package com.example.wishlist.infrastructure;

import com.example.wishlist.domain.CartItem;
import com.example.wishlist.domain.CartItemRepository;

import java.util.ArrayList;
import java.util.List;

public class ListCartItemRepository implements CartItemRepository {
    private List<CartItem> items = new ArrayList<>();
    private long nextId = 1;

    @Override
    public CartItem add(CartItem cartItem) {
        cartItem.setId(nextId++);
        items.add(cartItem);
        return cartItem;
    }

    @Override
    public CartItem findById(Long id) {
        for (CartItem item : items) {
            if (item.getId().equals(id)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public CartItem findByWishItemId(Long wishItemId) {
        for (CartItem item : items) {
            if (item.getWishItemId().equals(wishItemId)) {
                return item;
            }
        }
        return null;
    }

    @Override
    public List<CartItem> findAll() {
        return new ArrayList<>(items);
    }

    @Override
    public CartItem update(CartItem cartItem) {
        return cartItem;
    }

    @Override
    public boolean delete(Long id) {
        return items.removeIf(item -> item.getId().equals(id));
    }
}
