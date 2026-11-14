package com.example.wishlist.presentation;

import com.example.wishlist.infrastructure.DummyJsonClient;
import com.example.wishlist.infrastructure.FakeDummyJsonClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@ActiveProfiles("test")
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class ApiStatusTest {

    @TestConfiguration
    static class FakeClientConfig {
        @Bean
        @Primary
        DummyJsonClient fakeDummyJsonClient() {
            FakeDummyJsonClient client = new FakeDummyJsonClient();
            client.put(1, "Mascara", "9.99", 3);
            return client;
        }
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private DummyJsonClient client;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void clearTables() {
        jdbcTemplate.execute("TRUNCATE TABLE cart_items");
        jdbcTemplate.execute("TRUNCATE TABLE collection_items");
    }

    private void importExternal(long externalId) throws Exception {
        mockMvc.perform(post("/wish/items/import")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"externalId\": " + externalId + "}"))
                .andExpect(status().isOk());
    }

    private void addToCart(long wishItemId, int quantity) throws Exception {
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"wishItemId\": " + wishItemId + ", \"quantity\": " + quantity + "}"))
                .andExpect(status().isOk());
    }

    @Test
    @DisplayName("400: 검색어 공백, limit 범위 밖, skip 음수 (기본값은 limit 10, skip 0)")
    void searchValidation() throws Exception {
        mockMvc.perform(get("/external-products/search").param("q", "  "))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("검색어(q)를 입력해 주세요."));
        mockMvc.perform(get("/external-products/search"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/external-products").param("limit", "21"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/external-products").param("limit", "0"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/external-products").param("skip", "-1"))
                .andExpect(status().isBadRequest());

        mockMvc.perform(get("/external-products"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].price").value(9990));
        FakeDummyJsonClient fake = (FakeDummyJsonClient) client;
        org.junit.jupiter.api.Assertions.assertEquals(10, fake.lastLimit);
        org.junit.jupiter.api.Assertions.assertEquals(0, fake.lastSkip);
    }

    @Test
    @DisplayName("200 + 빈 목록: 검색 결과 없음")
    void emptySearch() throws Exception {
        mockMvc.perform(get("/external-products/search").param("q", "nothing"))
                .andExpect(status().isOk())
                .andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("404: 없는 외부 상품, 로컬 상품, 장바구니 행")
    void notFound() throws Exception {
        mockMvc.perform(get("/external-products/999")).andExpect(status().isNotFound());
        mockMvc.perform(post("/wish/items/import")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"externalId\": 999}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"wishItemId\": 99, \"quantity\": 1}"))
                .andExpect(status().isNotFound());
        mockMvc.perform(patch("/cart/items/99")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"quantity\": 1}"))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("409: 중복 가져오기, 수량 합산 초과, 재고 초과")
    void conflict() throws Exception {
        importExternal(1);
        mockMvc.perform(post("/wish/items/import")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"externalId\": 1}"))
                .andExpect(status().isConflict());

        addToCart(1, 3);
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"wishItemId\": 1, \"quantity\": 1}"))
                .andExpect(status().isConflict());

        mockMvc.perform(get("/cart"))
                .andExpect(jsonPath("$.items[0].quantity").value(3));
    }

    @Test
    @DisplayName("400: DTO 검증 실패 (수량 6, 수량 누락, JSON 형식 오류)")
    void bodyValidation() throws Exception {
        importExternal(1);
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"wishItemId\": 1, \"quantity\": 6}"))
                .andExpect(status().isBadRequest())
                .andExpect(content().string("수량은 1개부터 5개까지 가능합니다."));
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"wishItemId\": 1}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/cart/items")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"wishItemId\": 1, \"quantity\": \"abc\"}"))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/wish/items/import")
                        .contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("미리보기: 110,000원 표와 같고, 모르는 쿠폰, 조건 미달 쿠폰은 400")
    void preview() throws Exception {
        mockMvc.perform(post("/wish/items").param("name", "monitor").param("price", "55000"))
                .andExpect(status().isOk());
        addToCart(1, 2);

        mockMvc.perform(get("/cart/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.coupon").value("NONE"))
                .andExpect(jsonPath("$.finalAmount").value(110000));
        mockMvc.perform(get("/cart/preview").param("couponCode", "PERCENT10"))
                .andExpect(jsonPath("$.subtotal").value(110000))
                .andExpect(jsonPath("$.discount").value(11000))
                .andExpect(jsonPath("$.discountedSubtotal").value(99000))
                .andExpect(jsonPath("$.shipping").value(5000))
                .andExpect(jsonPath("$.finalAmount").value(104000));
        mockMvc.perform(get("/cart/preview").param("couponCode", "FIXED15000"))
                .andExpect(jsonPath("$.finalAmount").value(100000));
        mockMvc.perform(get("/cart/preview").param("couponCode", "WRONG"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("빈 장바구니 미리보기: 모두 0, NONE 외 쿠폰은 400")
    void emptyPreview() throws Exception {
        mockMvc.perform(get("/cart/preview"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.finalAmount").value(0))
                .andExpect(jsonPath("$.shipping").value(0));
        mockMvc.perform(get("/cart/preview").param("couponCode", "PERCENT10"))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("502: 외부 서버 연결 실패, 위시리스트는 바뀌지 않는다")
    void badGateway() throws Exception {
        ((FakeDummyJsonClient) client).down = true;

        mockMvc.perform(get("/external-products")).andExpect(status().isBadGateway());
        mockMvc.perform(post("/wish/items/import")
                        .contentType(MediaType.APPLICATION_JSON).content("{\"externalId\": 1}"))
                .andExpect(status().isBadGateway());
        mockMvc.perform(get("/wish/items")).andExpect(content().json("[]"));
    }
}
