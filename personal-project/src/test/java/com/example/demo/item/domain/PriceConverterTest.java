package com.example.demo.item.domain;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PriceConverterTest {

    @Test
    void 외부_가격_1단위를_1000원으로_환산한다() {
        assertEquals(9990, PriceConverter.toWon(new BigDecimal("9.99")));
        assertEquals(10000, PriceConverter.toWon(new BigDecimal("10")));
    }

    @Test
    void 원_미만은_HALF_UP으로_반올림한다() {
        assertEquals(1, PriceConverter.toWon(new BigDecimal("0.0005")));
        assertEquals(0, PriceConverter.toWon(new BigDecimal("0.0004")));
        assertEquals(1235, PriceConverter.toWon(new BigDecimal("1.2345")));
    }

    @Test
    void 가격이_없으면_예외가_발생한다() {
        assertThrows(IllegalArgumentException.class, () -> PriceConverter.toWon(null));
    }
}
