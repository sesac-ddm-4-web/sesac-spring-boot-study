package com.example.wishlist.management.domain.external;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class ExternalPriceConverter {
    private static final BigDecimal RATE = BigDecimal.valueOf(1_000);

    private ExternalPriceConverter() {}

    public static int toWon(BigDecimal externalPrice){
        return externalPrice.multiply(RATE)
                // 소수 -> 원 단위 반올림
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }
}
