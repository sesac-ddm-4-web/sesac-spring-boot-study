package com.example.personal_project.domain.product.model;

import java.math.BigDecimal;
import java.math.RoundingMode;

public class PriceConverter {

    public long toWon(BigDecimal price) {
        if (price == null || price.signum() < 0) {
            throw new IllegalArgumentException(
                    "외부 가격은 0 이상이어야 합니다."
            );
        }

        return price.multiply(BigDecimal.valueOf(1000))
                .setScale(0, RoundingMode.HALF_UP)
                .longValueExact();
    }
}
