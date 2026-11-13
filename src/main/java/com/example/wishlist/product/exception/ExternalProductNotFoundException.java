package com.example.wishlist.product.exception;

import com.example.wishlist.common.exception.NotFoundException;

public class ExternalProductNotFoundException extends NotFoundException {

    public ExternalProductNotFoundException(long externalId) {
        super("외부 상품을 찾을 수 없습니다. externalId=" + externalId);
    }
}