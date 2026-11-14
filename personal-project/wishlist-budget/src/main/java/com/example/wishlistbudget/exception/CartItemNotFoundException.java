package com.example.wishlistbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CartItemNotFoundException extends RuntimeException {

    public CartItemNotFoundException(Long id) {
        super("장바구니 상품을 찾을 수 없습니다. ID: " + id);
    }
}