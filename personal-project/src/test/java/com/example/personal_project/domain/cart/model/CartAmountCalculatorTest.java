package com.example.personal_project.domain.cart.model;

import com.example.personal_project.domain.item.model.SourceType;
import com.example.personal_project.global.exception.CartAmountException;
import com.example.personal_project.global.exception.InvalidCouponException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

class CartAmountCalculatorTest {
    private final CartAmountCalculator calculator = new CartAmountCalculator();
    private CartItem item(long price, int quantity) {
        return new CartItem(1L, 1L, "상품", price, SourceType.MANUAL, null, quantity);
    }

    @ParameterizedTest
    @CsvSource({
        "110000,NONE,0,0,110000", "110000,PERCENT10,11000,5000,104000",
        "110000,FIXED15000,15000,5000,100000", "100000,FIXED15000,15000,5000,90000",
        "300000,PERCENT10,20000,0,280000", "10009,PERCENT10,1000,5000,14009",
        "99999,NONE,0,5000,104999", "100000,NONE,0,0,100000",
        "114999,FIXED15000,15000,5000,104999", "115000,FIXED15000,15000,0,100000",
        "111110,PERCENT10,11111,5000,104999", "111111,PERCENT10,11111,0,100000",
        "199999,PERCENT10,19999,0,180000", "200000,PERCENT10,20000,0,180000",
        "0,NONE,0,5000,5000"
    })
    void amountRules(long subtotal, Coupon coupon, long discount, long shipping, long finalAmount) {
        assertEquals(new CartPreview(subtotal, discount, subtotal - discount, shipping, finalAmount, coupon),
                calculator.calculate(List.of(item(subtotal, 1)), coupon));
    }

    @Test
    void quantityAndMultipleLinesAreIncluded() {
        var items = List.of(item(30000, 3), item(10000, 2));
        var before = List.copyOf(items);
        assertEquals(110000, calculator.calculate(items, Coupon.NONE).subtotal());
        assertEquals(before, items);
    }

    @Test
    void emptyCartAndUnavailableCoupons() {
        assertEquals(new CartPreview(0,0,0,0,0,Coupon.NONE), calculator.calculate(List.of(), Coupon.NONE));
        for (Coupon coupon : List.of(Coupon.PERCENT10, Coupon.FIXED15000)) {
            assertThrows(InvalidCouponException.class, () -> calculator.calculate(List.of(), coupon));
        }
        assertThrows(InvalidCouponException.class,
                () -> calculator.calculate(List.of(item(99999,1)), Coupon.FIXED15000));
        assertEquals(Coupon.NONE, Coupon.from(null));
        for (String code : List.of("", " ", "none", "UNKNOWN")) {
            assertThrows(InvalidCouponException.class, () -> Coupon.from(code));
        }
    }

    @Test
    void overflowIsRejectedInsteadOfWrapping() {
        assertThrows(CartAmountException.class,
                () -> calculator.calculate(List.of(item(Long.MAX_VALUE,2)), Coupon.NONE));
        assertThrows(CartAmountException.class,
                () -> calculator.calculate(List.of(item(Long.MAX_VALUE,1),item(1,1)), Coupon.NONE));
    }
    @ParameterizedTest
    @CsvSource({"110000,FIXED15000,100000", "9,NONE,5009",
        "0,NONE,5000", "10,PERCENT10,5009", "150000,PERCENT10,135000",
        "300000,PERCENT10,280000"})
    void bestCouponIncludesShippingAndPreservesTieOrder(long subtotal, Coupon coupon, long amount) {
        var items = List.of(item(subtotal,1));
        var before = List.copyOf(items);
        var result = calculator.bestCoupon(items);
        assertEquals(coupon,result.coupon());
        assertEquals(amount,result.finalAmount());
        assertEquals(before,items);
    }

    @Test void emptyBestCouponIsNone() {
        assertEquals(new CartPreview(0,0,0,0,0,Coupon.NONE),calculator.bestCoupon(List.of()));
    }
}
