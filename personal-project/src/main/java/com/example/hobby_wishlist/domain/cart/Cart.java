package com.example.hobby_wishlist.domain.cart;

import com.example.hobby_wishlist.domain.wishlist.WishlistItem;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;

public class Cart {
    private static final int MAX_ITEM_CAPACITY = 20;
    private final List<CartItem> items = new ArrayList<>();

    private Optional<CartItem> findByWishlistItemId(Long wishlistId) {
        return items.stream()
                .filter(item -> item.hasSameWishlistId(wishlistId))
                .findFirst();
    }

    private Optional<CartItem> findByCartItemId(Long cartItemId) {
        return items.stream()
                .filter(item -> item.hasSameId(cartItemId))
                .findFirst();
    }

    public CartItem add(WishlistItem wishlistItem, int quantity) {
        return findByWishlistItemId(wishlistItem.getId())
                // 장바구니에 상품이 존재하는 경우 수량을 더함
                .map(item -> {
                    item.addQuantity(quantity);
                    return item;
                })
                // 장바구니에 상품을 새로 추가
                .orElseGet(() -> {
                    if (items.size() >= MAX_ITEM_CAPACITY) {
                        throw new CartRuleViolationException("장바구니에는 최대 20개까지 담을 수 있습니다.");
                    }
                    CartItem item = CartItem.from(null, wishlistItem, quantity);
                    items.add(item);
                    return item;
                });
    }

    public CartItem changeQuantity(Long cartItemId, int quantity) {
        return findByCartItemId(cartItemId)
                .map(item -> {
                    item.changeQuantity(quantity);
                    return item;
                })
                .orElseThrow(() -> new CartItemNotFoundException("장바구니에 항목이 없습니다."));
    }

    public void remove(Long cartItemId) {
        if (!items.removeIf(item -> item.hasSameId(cartItemId))) {
            throw new CartItemNotFoundException("장바구니에 항목이 없습니다.");
        }
    }

    public List<CartItem> getItems() {
        return List.copyOf(items);
    }

    public void assignIds(Supplier<Long> idGenerator) {
        for (var item : items) {
            if (item.getId() == null) {
                item.setId(idGenerator.get());
            }
        }
    }
}
