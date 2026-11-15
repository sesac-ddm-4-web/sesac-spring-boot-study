package com.example.demo.cart.domain;

import com.example.demo.global.exception.BadRequestException;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class PricePreviewTest {

    private void assertPreview(PricePreview preview, long discount, long discountedSubtotal, long shipping, long finalAmount) {
        assertEquals(discount, preview.discount());
        assertEquals(discountedSubtotal, preview.discountedSubtotal());
        assertEquals(shipping, preview.shipping());
        assertEquals(finalAmount, preview.finalAmount());
    }

    @Test
    void 상품_합계_110000원에_쿠폰이_없으면_무료배송이다() {
        PricePreview preview = PricePreview.of(110_000, Coupon.NONE);

        assertEquals(110_000, preview.subtotal());
        assertPreview(preview, 0, 110_000, 0, 110_000);
        assertEquals(Coupon.NONE, preview.coupon());
    }

    @Test
    void PERCENT10은_할인_후_100000원_미만이면_배송비_5000원이_붙는다() {
        PricePreview preview = PricePreview.of(110_000, Coupon.PERCENT10);

        assertPreview(preview, 11_000, 99_000, 5_000, 104_000);
    }

    @Test
    void FIXED15000은_15000원을_할인하고_할인_후_금액으로_배송비를_정한다() {
        PricePreview preview = PricePreview.of(110_000, Coupon.FIXED15000);

        assertPreview(preview, 15_000, 95_000, 5_000, 100_000);
    }

    @Test
    void PERCENT10_할인액은_원_미만을_버린다() {
        assertEquals(1_234, PricePreview.of(12_345, Coupon.PERCENT10).discount());
    }

    @Test
    void PERCENT10_할인액은_최대_20000원이다() {
        assertEquals(20_000, PricePreview.of(200_000, Coupon.PERCENT10).discount());
        assertEquals(20_000, PricePreview.of(300_000, Coupon.PERCENT10).discount());
    }

    @Test
    void FIXED15000은_상품_합계가_100000원_이상일_때만_사용할_수_있다() {
        assertThrows(BadRequestException.class, () -> PricePreview.of(99_999, Coupon.FIXED15000));
        assertEquals(15_000, PricePreview.of(100_000, Coupon.FIXED15000).discount());
    }

    @Test
    void 할인_후_금액이_정확히_100000원이면_무료배송이다() {
        assertEquals(0, PricePreview.of(115_000, Coupon.FIXED15000).shipping());
        assertEquals(5_000, PricePreview.of(99_999, Coupon.NONE).shipping());
    }

    @Test
    void 빈_장바구니는_모든_금액이_0이다() {
        PricePreview preview = PricePreview.of(0, Coupon.NONE);

        assertEquals(0, preview.subtotal());
        assertPreview(preview, 0, 0, 0, 0);
    }

    @Test
    void 빈_장바구니에는_NONE만_사용할_수_있다() {
        assertThrows(BadRequestException.class, () -> PricePreview.of(0, Coupon.PERCENT10));
        assertThrows(BadRequestException.class, () -> PricePreview.of(0, Coupon.FIXED15000));
    }
}
