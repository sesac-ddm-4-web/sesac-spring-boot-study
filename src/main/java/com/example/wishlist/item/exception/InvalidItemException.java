package com.example.wishlist.item.exception;

import com.example.wishlist.common.exception.BadRequestException;

public class InvalidItemException extends BadRequestException {

    public InvalidItemException(String message) {
        super(message);
    }
}