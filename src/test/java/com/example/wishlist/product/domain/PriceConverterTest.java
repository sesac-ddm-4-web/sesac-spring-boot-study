package com.example.wishlist.product.domain;

import static org.assertj.core.api.Assertions.assertThat;

import java.math.BigDecimal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

public class PriceConverterTest {

    private int toWon(String price) {
        return PriceConverter.toWon(new BigDecimal(price));
    }

    @Test
    @DisplayName("1단위를 1000원으로 환산한다")
    void convert() {
        assertThat(toWon("9.99")).isEqualTo(9990);
        assertThat(toWon("5")).isEqualTo(5000);
        assertThat(toWon("0")).isZero();
    }

    @Test
    @DisplayName("원 미만은 HALF_UP으로 반올림한다 (0.5 이상 올림)")
    void halfUp() {
        assertThat(toWon("1.2345")).isEqualTo(1235);   // 1234.5 → 1235 (HALF_EVEN이면 1234)
        assertThat(toWon("1.2344")).isEqualTo(1234);   // 1234.4 → 1234
        assertThat(toWon("0.0005")).isEqualTo(1);      // 0.5 → 1
        assertThat(toWon("0.0004")).isZero();          // 0.4 → 0
    }

    @Test
    @DisplayName("ExternalProduct도 같은 규칙으로 원 단위 가격을 계산한다")
    void externalProductPriceInWon() {
        ExternalProduct product = new ExternalProduct(1L, "Essence Mascara", new BigDecimal("9.99"), "beauty", "t", 99);

        assertThat(product.priceInWon()).isEqualTo(9990);
    }
}
