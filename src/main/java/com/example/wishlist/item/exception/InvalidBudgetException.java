package com.example.wishlist.item.exception;

import com.example.wishlist.common.exception.BadRequestException;

public class InvalidBudgetException extends BadRequestException {

    public InvalidBudgetException(Integer budget) {
        super("예산은 0 이상의 정수여야 합니다. budget=" + budget);
    }
}