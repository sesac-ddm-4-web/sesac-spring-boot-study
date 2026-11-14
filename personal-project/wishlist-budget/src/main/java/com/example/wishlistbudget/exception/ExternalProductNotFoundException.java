package com.example.wishlistbudget.exception;

import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.ResponseStatus;

@ResponseStatus(HttpStatus.NOT_FOUND)
public class ExternalProductNotFoundException extends RuntimeException {

    public ExternalProductNotFoundException(Long externalId) {
        super("존재하지 않는 외부 상품입니다. ID: " + externalId);
    }
}