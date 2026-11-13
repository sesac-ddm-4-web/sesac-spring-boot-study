package com.example.wishlist.product.exception;

import com.example.wishlist.common.exception.ExternalServiceException;

public class ExternalApiException extends ExternalServiceException {

    public ExternalApiException(String message) {
        super(message);
    }
}