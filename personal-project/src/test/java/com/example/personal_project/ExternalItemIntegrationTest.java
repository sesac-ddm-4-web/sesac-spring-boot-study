package com.example.personal_project;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;

import java.io.IOException;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ExternalItemIntegrationTest extends ApiIntegrationTestSupport {
    @AfterEach
    void verifyExternalRequests() {
        externalApi.server.verify();
    }

    @Test
    @DisplayName("외부 목록 기본 페이지와 검색어·페이지 전달 및 응답 매핑을 검증한다")
    void listAndSearch() throws Exception {
        expectProductPage("/products?limit=10&skip=0");
        expectProductPage("/products/search?q=phone&limit=2&skip=3");
        mockMvc.perform(get("/external-item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.products[0].id").value(42))
                .andExpect(jsonPath("$.products[0].title").value("Phone"))
                .andExpect(jsonPath("$.products[0].price").value(9.99))
                .andExpect(jsonPath("$.products[0].stock").value(2))
                .andExpect(jsonPath("$.total").value(1))
                .andExpect(jsonPath("$.limit").value(10))
                .andExpect(jsonPath("$.skip").value(0));
        mockMvc.perform(get("/external-item/search").param("q", "phone")
                        .param("limit", "2").param("skip", "3"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.products", hasSize(1)));
        mockMvc.perform(get("/item")).andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("검색 기본 페이지 값과 외부 목록 페이지 경계값을 허용한다")
    void paginationDefaultsAndBoundaries() throws Exception {
        expectProductPage("/products/search?q=phone&limit=10&skip=0");
        expectProductPage("/products?limit=1&skip=0");
        expectProductPage("/products?limit=20&skip=0");
        mockMvc.perform(get("/external-item/search").param("q", "phone"))
                .andExpect(status().isOk());
        for (String limit : new String[]{"1", "20"}) {
            mockMvc.perform(get("/external-item").param("limit", limit))
                    .andExpect(status().isOk());
        }
    }

    @ParameterizedTest
    @CsvSource({"limit,0", "limit,21", "skip,-1"})
    @DisplayName("목록·검색의 잘못된 페이지 값은 외부 호출 전에 400이다")
    void rejectInvalidPagination(String key, String value) throws Exception {
        for (String path : new String[]{"/external-item", "/external-item/search"}) {
            mockMvc.perform(get(path).param("q", "phone").param(key, value))
                    .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
        }
    }

    @ParameterizedTest
    @ValueSource(strings = {"", "   "})
    @DisplayName("빈 검색어·공백 검색어는 외부 호출 전에 400이다")
    void rejectBlankSearch(String q) throws Exception {
        mockMvc.perform(get("/external-item/search").param("q", q))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("검색어는 비어있을 수 없습니다"));
    }

    @Test
    @DisplayName("외부 상품을 내부 ID로 등록하고 가격을 반올림하며 중복 등록을 거부한다")
    void importAndRejectDuplicate() throws Exception {
        expectProduct(42, "9.9905", 2);
        expectProduct(42, "9.9905", 2);
        mockMvc.perform(post("/item/external/42"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("Phone"))
                .andExpect(jsonPath("$.price").value(9991))
                .andExpect(jsonPath("$.stock").value(2));
        mockMvc.perform(post("/item/external/42"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("이미 추가한 외부 item입니다"));
        mockMvc.perform(get("/item")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("외부 상품 재고는 추가·수정 때 검사하고 성공해도 차감하지 않는다")
    void stockChecksWithoutReservation() throws Exception {
        expectProduct(42, "10", 2);
        mockMvc.perform(post("/item/external/42")).andExpect(status().isOk());
        mockMvc.perform(post("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"collectionItemId\":1,\"amount\":3}"))
                .andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("재고가 부족합니다"));
        mockMvc.perform(post("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"collectionItemId\":1,\"amount\":2}"))
                .andExpect(status().isOk()).andExpect(jsonPath("$.stock").value(2));
        mockMvc.perform(patch("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"collectionItemId\":1,\"amount\":3}"))
                .andExpect(status().isConflict());
        mockMvc.perform(get("/shopping-cart"))
                .andExpect(jsonPath("$[0].amount").value(2));
        mockMvc.perform(get("/item/1")).andExpect(jsonPath("$.stock").value(2));
    }

    @Test
    @DisplayName("재고 0인 상품도 추천되지만 장바구니에는 담을 수 없다")
    void outOfStockRecommendation() throws Exception {
        expectProduct(42, "10", 0);
        mockMvc.perform(post("/item/external/42")).andExpect(status().isOk());
        mockMvc.perform(get("/item/recommendations").param("budget", "10000"))
                .andExpect(status().isOk()).andExpect(jsonPath("$[0].stock").value(0));
        mockMvc.perform(post("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"collectionItemId\":1,\"amount\":1}"))
                .andExpect(status().isConflict());
    }

    @ParameterizedTest
    @ValueSource(longs = {0, -1})
    @DisplayName("외부 상품 ID는 양수여야 한다")
    void rejectInvalidExternalId(long id) throws Exception {
        mockMvc.perform(post("/item/external/{id}", id))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("상품id는 양수입니다"));
    }

    @Test
    @DisplayName("외부 상품도 내부 상품과 같은 이름·가격 검증을 적용한다")
    void validateImportedProduct() throws Exception {
        externalApi.server.expect(requestTo("https://dummyjson.com/products/42"))
                .andRespond(withSuccess("""
                        {"id":42,"title":"","price":10,"stock":2}
                        """, MediaType.APPLICATION_JSON));
        mockMvc.perform(post("/item/external/42"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("name, 상품 이름은 1~40글자입니다."));
        mockMvc.perform(get("/item")).andExpect(content().json("[]"));
    }

    @ParameterizedTest
    @CsvSource({"404,404", "400,502", "500,502"})
    @DisplayName("외부 단건 404는 404로, 그 밖의 외부 오류는 502로 변환한다")
    void translateImportErrors(int externalStatus, int expectedStatus) throws Exception {
        externalApi.server.expect(requestTo("https://dummyjson.com/products/42"))
                .andRespond(withStatus(HttpStatus.valueOf(externalStatus)));
        mockMvc.perform(post("/item/external/42"))
                .andExpect(status().is(expectedStatus)).andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @ParameterizedTest
    @ValueSource(strings = {"/external-item", "/external-item/search"})
    @DisplayName("외부 목록·검색의 404도 502로 변환한다")
    void translatePageErrors(String path) throws Exception {
        String uri = path.endsWith("search")
                ? "/products/search?q=phone&limit=10&skip=0" : "/products?limit=10&skip=0";
        externalApi.server.expect(requestTo("https://dummyjson.com" + uri))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        mockMvc.perform(get(path).param("q", "phone"))
                .andExpect(status().isBadGateway()).andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    @DisplayName("외부 통신 실패는 502이다")
    void translateNetworkFailure() throws Exception {
        externalApi.server.expect(requestTo("https://dummyjson.com/products/42"))
                .andRespond(withException(new IOException("simulated connection failure")));
        mockMvc.perform(post("/item/external/42"))
                .andExpect(status().isBadGateway())
                .andExpect(jsonPath("$.errors[0]").value("외부 상품 정보를 가져오지 못했습니다"));
    }

    private void expectProduct(long id, String price, int stock) {
        externalApi.server.expect(requestTo("https://dummyjson.com/products/" + id))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("{\"id\":%d,\"title\":\"Phone\",\"price\":%s,\"stock\":%d}"
                        .formatted(id, price, stock), MediaType.APPLICATION_JSON));
    }

    private void expectProductPage(String path) {
        externalApi.server.expect(requestTo("https://dummyjson.com" + path))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"products":[{"id":42,"title":"Phone","price":9.99,"stock":2}],
                         "total":1,"skip":0,"limit":10}
                        """, MediaType.APPLICATION_JSON));
    }
}
