package com.example.wishlist.domain;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PriceConverterTest {

    @Test
    @DisplayName("외부 price 1단위는 1,000원: 9.99 → 9,990원")
    void convert() {
        assertEquals(9990, PriceConverter.toWon(new BigDecimal("9.99")));
        assertEquals(1000, PriceConverter.toWon(new BigDecimal("1")));
        assertEquals(0, PriceConverter.toWon(new BigDecimal("0")));
    }

    @Test
    @DisplayName("HALF_UP 반올림: 0.5원은 올리고 0.4원은 버린다")
    void roundHalfUp() {
        assertEquals(1235, PriceConverter.toWon(new BigDecimal("1.2345")));
        assertEquals(1234, PriceConverter.toWon(new BigDecimal("1.2344")));
    }
}
