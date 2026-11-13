package com.example.wishlist.product.domain;

import com.example.wishlist.product.exception.ExternalApiException;
import java.math.BigDecimal;
import java.math.RoundingMode;

// 외부 price -> 원으로 고정 환산 (1000원)
public final class PriceConverter {

    public static final BigDecimal WON_PER_UNIT = BigDecimal.valueOf(1_000);

    private PriceConverter() {}

    public static int toWon(BigDecimal externalPrice) {
        try {
            return externalPrice
                .multiply(WON_PER_UNIT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
        } catch (ArithmeticException e) {
            throw new ExternalApiException("외부 상품 가격을 원 단위로 바꿀 수 없습니다: " + externalPrice);
        }
    }
}
