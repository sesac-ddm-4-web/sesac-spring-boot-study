package com.example.wishlist.item.exception;

import com.example.wishlist.common.exception.NotFoundException;

public class ItemNotFoundException extends NotFoundException {

    public ItemNotFoundException(Long id) {
        super("아이템을 찾을 수 없습니다. id=" + id);
    }
}