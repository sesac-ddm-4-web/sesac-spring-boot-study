package com.example.personal_project;

import com.example.personal_project.domain.entity.ShoppingCart;
import com.example.personal_project.domain.repository.ShoppingCartRepository;
import com.example.personal_project.presentation.dto.shoppingCart.ShoppingCartRequest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;

import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class SubtotalIntegrationTest extends ApiIntegrationTestSupport {
    @Autowired private ShoppingCartRepository carts;

    @Test
    @DisplayName("브라우저용 POST 합계 조회도 기존 GET과 같은 계산 결과를 반환한다")
    void calculateWithPost() throws Exception {
        addLine(50000, 2);
        String body = "{\"couponCode\":\"PERCENT10\"}";
        String expected = mockMvc.perform(get("/shopping-cart/subtotal")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andReturn().getResponse().getContentAsString();
        mockMvc.perform(post("/shopping-cart/subtotal")
                        .contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk()).andExpect(content().json(expected))
                .andExpect(jsonPath("$.finalPrice").value(95000));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"couponCode\":null}"})
    @DisplayName("쿠폰 생략 또는 null이면 빈 장바구니의 금액과 배송비는 모두 0이다")
    void emptyCartWithoutCoupon(String body) throws Exception {
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(0))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.discountedSubtotal").value(0))
                .andExpect(jsonPath("$.shipping").value(0))
                .andExpect(jsonPath("$.finalPrice").value(0))
                .andExpect(jsonPath("$.coupon").value(nullValue()));
    }

    @Test
    @DisplayName("쿠폰 미사용도 합계가 33,000원이면 배송비 5,000원을 부과한다")
    void sumLinesWithoutCoupon() throws Exception {
        addLine(12000, 2);
        addLine(3000, 3);
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(33000))
                .andExpect(jsonPath("$.discount").value(0))
                .andExpect(jsonPath("$.discountedSubtotal").value(33000))
                .andExpect(jsonPath("$.shipping").value(5000))
                .andExpect(jsonPath("$.finalPrice").value(38000));
    }

    @ParameterizedTest
    @CsvSource({
            "NONE,      0,      0,     0,      0,    0",
            "NONE,      99999,  0,     99999,  5000, 104999",
            "NONE,      100000, 0,     100000, 0,    100000",
            "PERCENT10, 1,      0,     1,      5000, 5001",
            "PERCENT10, 999,    99,    900,    5000, 5900",
            "PERCENT10, 100000, 10000, 90000,  5000, 95000",
            "PERCENT10, 111110, 11111, 99999,  5000, 104999",
            "PERCENT10, 111111, 11111, 100000, 0,    100000",
            "PERCENT10, 200000, 20000, 180000, 0,    180000",
            "PERCENT10, 300000, 20000, 280000, 0,    280000",
            "FIXED15000,100000, 15000, 85000,  5000, 90000",
            "FIXED15000,114999, 15000, 99999,  5000, 104999",
            "FIXED15000,115000, 15000, 100000, 0,    100000"
    })
    @DisplayName("쿠폰 할인·원 단위 버림·할인 한도·할인 후 무료배송 경계를 검증한다")
    void calculateCouponAndShipping(String coupon, int subtotal, long discount,
                                   long discounted, int shipping, long finalPrice) throws Exception {
        addLine(subtotal, 1);
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"%s\"}".formatted(coupon)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subtotal").value(subtotal))
                .andExpect(jsonPath("$.discount").value(discount))
                .andExpect(jsonPath("$.discountedSubtotal").value(discounted))
                .andExpect(jsonPath("$.shipping").value(shipping))
                .andExpect(jsonPath("$.finalPrice").value(finalPrice))
                .andExpect(jsonPath("$.coupon").value(coupon));
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "percent10", "UNKNOWN", " PERCENT10 "})
    @DisplayName("정의되지 않은 쿠폰 코드는 400이다")
    void rejectUnknownCoupon(String coupon) throws Exception {
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"%s\"}".formatted(coupon)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("존재하지 않는 쿠폰입니다"));
    }

    @ParameterizedTest
    @CsvSource({"PERCENT10,0", "FIXED15000,0", "FIXED15000,99999"})
    @DisplayName("쿠폰의 최소 사용 금액을 충족하지 못하면 400이다")
    void rejectIneligibleCoupon(String coupon, int subtotal) throws Exception {
        if (subtotal > 0) addLine(subtotal, 1);
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"couponCode\":\"%s\"}".formatted(coupon)))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("합계는 int 범위를 넘는 금액도 오버플로 없이 계산한다")
    void sumUsingLong() throws Exception {
        addLine(2_000_000_000, 5);
        mockMvc.perform(get("/shopping-cart/subtotal").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.subtotal").value(10_000_000_000L))
                .andExpect(jsonPath("$.finalPrice").value(10_000_000_000L));
    }

    private void addLine(int price, int amount) {
        long id = createItem(price);
        carts.add(ShoppingCart.createShoppingCart(items.findById(id), new ShoppingCartRequest(id, amount)));
    }
}
