package com.example.wishlist.cart.exception;

import com.example.wishlist.common.exception.NotFoundException;

public class CartItemNotFoundException extends NotFoundException {

    public CartItemNotFoundException(Long cartItemId) {
        super("장바구니 항목을 찾을 수 없습니다. cartItemId=" + cartItemId);
    }
}