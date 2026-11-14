package com.example.wishlistbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.CONFLICT)
public class DuplicateExternalProductException extends RuntimeException {

    public DuplicateExternalProductException(Long externalId) {
        super("이미 위시리스트에 등록된 외부 상품입니다. ID: " + externalId);
    }
}