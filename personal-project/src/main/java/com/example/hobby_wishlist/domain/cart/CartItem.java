package com.example.hobby_wishlist.domain.cart;

import com.example.hobby_wishlist.domain.wishlist.WishlistItem;

public class CartItem {
    public static final int MAX_QUANTITY = 5;

    private Long id;
    private final Long wishlistId;
    private final String name;
    private final Integer price;
    private final Integer stock;
    private Integer quantity;

    private CartItem(Long id, Long wishlistId, String name, Integer price, Integer stock, Integer quantity) {
        this.id = id;
        this.wishlistId = wishlistId;
        this.name = name;
        this.price = price;
        this.stock = stock;
        this.quantity = quantity;

        validateQuantity(quantity);
    }

    public static CartItem from(Long id, WishlistItem wishlistItem, Integer quantity) {
        return new CartItem(id, wishlistItem.getId(), wishlistItem.getName(), wishlistItem.getPrice(), wishlistItem.getStock(), quantity);
    }

    void setId(Long id) {
        this.id = id;
    }

    public Long getId() {
        return id;
    }

    public Long getWishlistId() {
        return wishlistId;
    }

    public String getName() {
        return name;
    }

    public Integer getPrice() {
        return price;
    }

    public Integer getStock() {
        return stock;
    }

    public Integer getQuantity() {
        return quantity;
    }

    public boolean hasSameId(Long cartId) {
        return this.id.equals(cartId);
    }

    public boolean hasSameWishlistId(Long wishlistId) {
        return this.wishlistId.equals(wishlistId);
    }

    private void validateQuantity(int quantity) {
        if (quantity < 1 || quantity > MAX_QUANTITY) {
            throw new CartRuleViolationException("장바구니에 담긴 수량은 5개 이하여야 합니다.");
        }
        if (stock != null && quantity > stock) {
            throw new CartRuleViolationException("재고(%d)를 초과할 수 없습니다.".formatted(stock));
        }
    }

    public void addQuantity(int q) {
        changeQuantity(quantity + q);
    }

    public void changeQuantity(int q) {
        validateQuantity(q);
        quantity = q;
    }

    public Long getSubtotal() {
        return (long) price * quantity;
    }
}
