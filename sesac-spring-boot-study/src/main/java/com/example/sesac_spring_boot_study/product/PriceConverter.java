package com.example.sesac_spring_boot_study.product;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceConverter {
    private static final BigDecimal WON_PER_UNIT = BigDecimal.valueOf(1000);

    private PriceConverter() {
    }

    public static long toWon(BigDecimal externalPrice) {
        if (externalPrice == null || externalPrice.signum() < 0) {
            throw new IllegalArgumentException("외부 가격이 올바르지 않습니다.");
        }
        return externalPrice.multiply(WON_PER_UNIT)
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
