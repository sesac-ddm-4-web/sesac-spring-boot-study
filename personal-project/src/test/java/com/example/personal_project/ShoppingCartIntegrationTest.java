package com.example.personal_project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.ResultActions;

import static org.hamcrest.Matchers.hasSize;
import static org.hamcrest.Matchers.nullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

class ShoppingCartIntegrationTest extends ApiIntegrationTestSupport {
    @Test
    @DisplayName("장바구니에 추가·조회·수량 교체·삭제할 수 있다")
    void cartLifecycle() throws Exception {
        mockMvc.perform(get("/shopping-cart")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
        long id = createItem(50000);
        add(id, 2).andExpect(status().isOk())
                .andExpect(jsonPath("$.collectionItemId").value(id))
                .andExpect(jsonPath("$.name").value("테스트 상품"))
                .andExpect(jsonPath("$.unitPrice").value(50000))
                .andExpect(jsonPath("$.stock").value(nullValue()))
                .andExpect(jsonPath("$.amount").value(2));
        mockMvc.perform(get("/shopping-cart")).andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(2));
        edit(id, 5).andExpect(status().isOk()).andExpect(jsonPath("$.amount").value(5));
        mockMvc.perform(get("/shopping-cart"))
                .andExpect(jsonPath("$[0].amount").value(5));
        mockMvc.perform(delete("/shopping-cart/{id}", id))
                .andExpect(status().isOk()).andExpect(content().string(""));
        mockMvc.perform(get("/shopping-cart")).andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("동일 상품을 다시 담으면 한 항목에 수량을 합산한다")
    void mergeDuplicate() throws Exception {
        long id = createItem(1000);
        add(id, 1).andExpect(status().isOk());
        add(id, 3).andExpect(status().isOk())
                .andExpect(jsonPath("$.amount").value(4));
        mockMvc.perform(get("/shopping-cart")).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].amount").value(4));
        add(id, 2).andExpect(status().isConflict());
        mockMvc.perform(get("/shopping-cart")).andExpect(jsonPath("$[0].amount").value(4));
    }

    @ParameterizedTest
    @ValueSource(ints = {-1, 0, 6})
    @DisplayName("추가·수정 수량은 1~5만 허용하며 실패한 수정은 기존 수량을 보존한다")
    void rejectInvalidAmounts(int amount) throws Exception {
        long id = createItem(1000);
        add(id, amount).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("장바구니 수량은 1~5입니다"));
        add(id, 2).andExpect(status().isOk());
        add(id, amount).andExpect(status().isConflict());
        edit(id, amount).andExpect(status().isConflict());
        mockMvc.perform(get("/shopping-cart")).andExpect(jsonPath("$[0].amount").value(2));
    }

    @ParameterizedTest
    @ValueSource(strings = {"{}", "{\"collectionItemId\":1}", "{\"amount\":1}"})
    @DisplayName("추가·수정의 필수 필드 누락은 400이다")
    void rejectMissingFields(String body) throws Exception {
        mockMvc.perform(post("/shopping-cart").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
        mockMvc.perform(patch("/shopping-cart").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.errors").isArray());
    }

    @Test
    @DisplayName("존재하지 않는 상품 추가와 장바구니 항목 수정·삭제는 404이다")
    void rejectUnknownItems() throws Exception {
        add(999L, 1).andExpect(status().isNotFound());
        long id = createItem(1000);
        edit(id, 1).andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]").value("장바구니에 해당 상품이 없습니다"));
        mockMvc.perform(delete("/shopping-cart/{id}", id)).andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("서로 다른 상품은 20개까지 담으며 삭제하면 새 상품을 담을 수 있다")
    void enforceCapacityAndAllowReplacement() throws Exception {
        long firstId = createItem(1000);
        add(firstId, 5).andExpect(status().isOk());
        for (int i = 1; i < 20; i++) {
            add(createItem(1000), 5).andExpect(status().isOk());
        }
        long nextId = createItem(2000);
        add(nextId, 1).andExpect(status().isConflict())
                .andExpect(jsonPath("$.errors[0]").value("장바구니 아이템은 최대 20개입니다"));
        edit(firstId, 1).andExpect(status().isOk());
        mockMvc.perform(get("/shopping-cart")).andExpect(jsonPath("$", hasSize(20)));
        mockMvc.perform(delete("/shopping-cart/{id}", firstId)).andExpect(status().isOk());
        add(nextId, 1).andExpect(status().isOk());
    }

    private ResultActions add(long id, int amount) throws Exception {
        return mockMvc.perform(post("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                .content(body(id, amount)));
    }

    private ResultActions edit(long id, int amount) throws Exception {
        return mockMvc.perform(patch("/shopping-cart").contentType(MediaType.APPLICATION_JSON)
                .content(body(id, amount)));
    }

    private String body(long id, int amount) {
        return "{\"collectionItemId\":%d,\"amount\":%d}".formatted(id, amount);
    }
}
