package com.example.demo.item.domain;

import java.math.BigDecimal;
import java.math.RoundingMode;

// 금액 규칙이기에 domain에 위치
public final class PriceConverter {

    // 고정 환산 비율
    private static final BigDecimal WON_PER_UNIT = BigDecimal.valueOf(1000);

    // 객체를 만들 일이 없는 클래스라서 생성자 막음
    private PriceConverter() {
    }

    public static int toWon(BigDecimal externalPrice) {
        if (externalPrice == null) {
            throw new IllegalArgumentException("외부 상품 가격이 없습니다.");
        }
        return externalPrice
                .multiply(WON_PER_UNIT)
                .setScale(0, RoundingMode.HALF_UP)
                .intValueExact();
    }
}
