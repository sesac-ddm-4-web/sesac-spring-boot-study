package com.example.wishlist.cart.exception;

import com.example.wishlist.cart.domain.CartItem;
import com.example.wishlist.common.exception.BadRequestException;

public class InvalidQuantityException extends BadRequestException {

    public InvalidQuantityException(int quantity) {
        super("수량은 " + CartItem.MIN_QUANTITY + "~" + CartItem.MAX_QUANTITY
            + " 사이여야 합니다. quantity=" + quantity);
    }
}