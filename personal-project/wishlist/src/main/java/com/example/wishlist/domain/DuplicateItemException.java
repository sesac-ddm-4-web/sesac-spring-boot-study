package com.example.wishlist.domain;

public class DuplicateItemException extends RuntimeException {
    public DuplicateItemException(Long externalId) {
        super("이미 위시리스트에 가져온 외부 상품입니다. (외부 상품 id: " + externalId + ")");
    }
}
