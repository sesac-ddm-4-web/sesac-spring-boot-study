package com.example.wishlistbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class CollectionItemNotFoundException extends RuntimeException {

    public CollectionItemNotFoundException(Long id) {
        super("해당 아이템을 찾을 수 없습니다. ID: " + id);
    }
}