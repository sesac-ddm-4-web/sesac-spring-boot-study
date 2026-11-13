package com.example.wishlist.item.exception;

import com.example.wishlist.common.exception.ConflictException;

public class DuplicateImportException extends ConflictException {

    public DuplicateImportException(long externalId) {
        super("이미 위시리스트에 가져온 외부 상품입니다. externalId=" + externalId);
    }
}
