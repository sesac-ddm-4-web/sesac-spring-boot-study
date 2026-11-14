package com.example.wishlist.domain;

public class CartItemNotFoundException extends RuntimeException {
    public CartItemNotFoundException(Long id) {
        super("해당 id의 장바구니 항목을 찾을 수 없습니다. (id: " + id + ")");
    }
}
