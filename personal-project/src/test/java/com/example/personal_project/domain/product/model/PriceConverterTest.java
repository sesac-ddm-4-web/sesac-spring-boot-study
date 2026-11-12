package com.example.personal_project.domain.product.model;

import java.math.BigDecimal;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import static org.junit.jupiter.api.Assertions.*;

class PriceConverterTest {
    private final PriceConverter converter = new PriceConverter();
    @ParameterizedTest
    @CsvSource({"9.99,9990", "0,0", "1.2344,1234", "1.2345,1235", "0.0005,1", "2147483.648,2147483648"})
    void convertsToWonWithHalfUp(String price, long expected) {
        assertEquals(expected, converter.toWon(new BigDecimal(price)));
    }
    @Test void rejectsMissingNegativeAndOverflowPrices() {
        assertThrows(IllegalArgumentException.class, () -> converter.toWon(null));
        assertThrows(IllegalArgumentException.class, () -> converter.toWon(new BigDecimal("-0.01")));
        assertThrows(ArithmeticException.class, () -> converter.toWon(new BigDecimal("9223372036854776")));
    }
}
