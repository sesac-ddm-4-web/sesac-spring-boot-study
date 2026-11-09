package com.example.wishlist.domain;

public class ItemNotFoundException extends RuntimeException {
    public ItemNotFoundException(Long id) {
        super("해당 id의 아이템을 찾을 수 없습니다. (id: " + id + ")");
    }
}
