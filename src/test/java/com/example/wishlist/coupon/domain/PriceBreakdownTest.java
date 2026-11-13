package com.example.wishlist.coupon.domain;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.wishlist.coupon.exception.InvalidCouponException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

/**
 * 쿠폰 할인 · 배송비 · 최종 금액 · 최적 쿠폰
 */
class PriceBreakdownTest {

    @Nested
    @DisplayName("과제 예시: 상품 합계 110,000원")
    class Example110000 {

        @Test
        @DisplayName("NONE: 할인 0원, 배송비 0원, 최종 110,000원")
        void none() {
            PriceBreakdown result = PriceBreakdown.calculate(110_000, Coupon.NONE);

            assertThat(result.discount()).isZero();
            assertThat(result.shipping()).isZero();
            assertThat(result.finalAmount()).isEqualTo(110_000L);
        }

        @Test
        @DisplayName("PERCENT10: 할인 11,000원, 할인 후 99,000원이라 배송비 5,000원, 최종 104,000원")
        void percent10() {
            PriceBreakdown result = PriceBreakdown.calculate(110_000, Coupon.PERCENT10);

            assertThat(result.discount()).isEqualTo(11_000L);
            assertThat(result.discountedSubtotal()).isEqualTo(99_000L);
            assertThat(result.shipping()).isEqualTo(5_000L);
            assertThat(result.finalAmount()).isEqualTo(104_000L);
        }

        @Test
        @DisplayName("FIXED15000: 할인 15,000원, 배송비 5,000원, 최종 100,000원")
        void fixed15000() {
            PriceBreakdown result = PriceBreakdown.calculate(110_000, Coupon.FIXED15000);

            assertThat(result.discount()).isEqualTo(15_000L);
            assertThat(result.shipping()).isEqualTo(5_000L);
            assertThat(result.finalAmount()).isEqualTo(100_000L);
        }
    }

    @Nested
    @DisplayName("쿠폰 규칙")
    class CouponRules {

        @Test
        @DisplayName("PERCENT10은 원 미만을 버리고, 최대 20,000원까지 할인한다")
        void percent10FloorAndCap() {
            assertThat(PriceBreakdown.calculate(12_345, Coupon.PERCENT10).discount()).isEqualTo(1_234L);
            assertThat(PriceBreakdown.calculate(200_000, Coupon.PERCENT10).discount()).isEqualTo(20_000L);
            assertThat(PriceBreakdown.calculate(300_000, Coupon.PERCENT10).discount()).isEqualTo(20_000L);
        }

        @Test
        @DisplayName("FIXED15000은 상품 합계 100,000원 이상일 때만 사용할 수 있다 (99,999원은 400)")
        void fixedThreshold() {
            assertThat(PriceBreakdown.calculate(100_000, Coupon.FIXED15000).discount()).isEqualTo(15_000L);
            assertThatThrownBy(() -> PriceBreakdown.calculate(99_999, Coupon.FIXED15000))
                .isInstanceOf(InvalidCouponException.class);
        }

        @Test
        @DisplayName("쿠폰 코드: 생략·빈 값은 NONE, 대소문자·앞뒤 공백 무시, 모르는 코드는 400")
        void parseCode() {
            assertThat(Coupon.from(null)).isEqualTo(Coupon.NONE);
            assertThat(Coupon.from("  ")).isEqualTo(Coupon.NONE);
            assertThat(Coupon.from(" percent10 ")).isEqualTo(Coupon.PERCENT10);
            assertThatThrownBy(() -> Coupon.from("HALF50")).isInstanceOf(InvalidCouponException.class);
        }
    }

    @Nested
    @DisplayName("배송비와 빈 장바구니")
    class Shipping {

        @Test
        @DisplayName("할인 후 금액이 100,000원 이상이면 무료, 미만이면 5,000원")
        void threshold() {
            assertThat(PriceBreakdown.calculate(100_000, Coupon.NONE).shipping()).isZero();
            assertThat(PriceBreakdown.calculate(99_999, Coupon.NONE).shipping()).isEqualTo(5_000L);
        }

        @Test
        @DisplayName("빈 장바구니는 모든 금액이 0이고, NONE 외의 쿠폰은 400")
        void emptyCart() {
            PriceBreakdown result = PriceBreakdown.calculate(0, Coupon.NONE);

            assertThat(result.subtotal()).isZero();
            assertThat(result.discount()).isZero();
            assertThat(result.shipping()).isZero();
            assertThat(result.finalAmount()).isZero();
            assertThatThrownBy(() -> PriceBreakdown.calculate(0, Coupon.PERCENT10))
                .isInstanceOf(InvalidCouponException.class);
            assertThatThrownBy(() -> PriceBreakdown.calculate(0, Coupon.FIXED15000))
                .isInstanceOf(InvalidCouponException.class);
        }
    }

    @Nested
    @DisplayName("최적 쿠폰: 배송비 포함 최종 금액이 가장 작은 쿠폰")
    class Best {

        @Test
        @DisplayName("110,000원이면 FIXED15000(100,000원)이 최저")
        void example() {
            CouponComparison comparison = CouponComparison.of(110_000);

            assertThat(comparison.best().coupon()).isEqualTo(Coupon.FIXED15000);
            assertThat(comparison.candidates()).extracting(PriceBreakdown::coupon)
                .containsExactly(Coupon.NONE, Coupon.PERCENT10, Coupon.FIXED15000);
        }

        @Test
        @DisplayName("사용할 수 없는 쿠폰은 후보에서 빠진다 (50,000원이면 FIXED15000 제외)")
        void onlyUsableCandidates() {
            assertThat(CouponComparison.of(50_000).candidates()).extracting(PriceBreakdown::coupon)
                .containsExactly(Coupon.NONE, Coupon.PERCENT10);
        }

        @Test
        @DisplayName("최종 금액이 같으면 NONE → PERCENT10 → FIXED15000 순서로 고른다")
        void tieBreak() {
            // 5원: PERCENT10 할인이 0원이라 NONE과 같음 → NONE
            assertThat(CouponComparison.of(5).best().coupon()).isEqualTo(Coupon.NONE);
            // 150,000원: PERCENT10·FIXED15000 둘 다 15,000원 할인, 135,000원 → PERCENT10
            assertThat(CouponComparison.of(150_000).best().coupon()).isEqualTo(Coupon.PERCENT10);
        }

        @Test
        @DisplayName("빈 장바구니는 NONE만 후보다")
        void empty() {
            CouponComparison comparison = CouponComparison.of(0);

            assertThat(comparison.best().coupon()).isEqualTo(Coupon.NONE);
            assertThat(comparison.candidates()).hasSize(1);
        }
    }
}