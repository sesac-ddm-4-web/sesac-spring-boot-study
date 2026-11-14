package com.example.wishlist.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceConverter {
    private static final BigDecimal WON_PER_UNIT = new BigDecimal("1000");

    private PriceConverter() {
    }

    public static int toWon(BigDecimal externalPrice) {
        return externalPrice
                .multiply(WON_PER_UNIT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }
}
