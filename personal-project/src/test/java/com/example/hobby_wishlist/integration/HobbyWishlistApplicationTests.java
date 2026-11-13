package com.example.hobby_wishlist.integration;

import com.example.hobby_wishlist.application.WishlistService;
import com.example.hobby_wishlist.dto.wishlist.CreateWishlistItemRequest;
import com.example.hobby_wishlist.dto.wishlist.WishlistItemResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import tools.jackson.databind.JsonNode;
import tools.jackson.databind.ObjectMapper;

import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
@ActiveProfiles("test")
class HobbyWishlistApplicationTests {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private ObjectMapper objectMapper;
    @Autowired
    private WishlistService wishlistService;

    @Test
    @DisplayName("정상 수동 등록 및 조회 테스트")
    void validInputTest() throws Exception {
        // 정상 데이터로 요청
        String jsonInput = """
                {
                	"name": "연필",
                	"price": 500
                }
                """;

        MvcResult result = mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(jsonInput))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.wishlistItemId").exists())
                .andExpect(jsonPath("$.name").value("연필"))
                .andExpect(jsonPath("$.price").value(500))
                .andReturn();

        JsonNode jsonNode = objectMapper.readTree(result.getResponse().getContentAsString());
        Long id = jsonNode.get("wishlistItemId").asLong();

        // 앞서 요청한 데이터의 id로 조회
        mockMvc.perform(get("/wishlist/{id}", id))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.wishlistItemId").value(id))
                .andExpect(jsonPath("$.name").value("연필"))
                .andExpect(jsonPath("$.price").value(500));
    }

    @Test
    @DisplayName("비정상 데이터 추가 요청 시 400으로 응답하고 기존 목록은 유지되어야 함")
    void invalidWishlistInputTest() throws Exception {
        // 정상 데이터 준비
        var before = createValidItems();

        // 비정상 데이터로 요청(빈 이름, 음수 가격, 불완전 데이터)
        String blankNameJson = """
                {
                	"name": " ",
                	"price": 500
                }
                """;
        String negativePriceJson = """
                {
                	"name": "연필",
                	"price": -500
                }
                """;
        String incompleteJson = """
                {
                	"price": 500
                }
                """;
        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(blankNameJson))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(negativePriceJson))
                .andExpect(status().isBadRequest());
        mockMvc.perform(post("/wishlist")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(incompleteJson))
                .andExpect(status().isBadRequest());

        // 기존 목록 유지 확인
        var after = wishlistService.getAll();
        assertEquals(before, after);
    }

    private List<WishlistItemResponse> createValidItems() {
        wishlistService.add(new CreateWishlistItemRequest("항목1", 500));
        wishlistService.add(new CreateWishlistItemRequest("항목2", 2000));
        wishlistService.add(new CreateWishlistItemRequest("항목3", 300));
        wishlistService.add(new CreateWishlistItemRequest("항목4", 500));
        wishlistService.add(new CreateWishlistItemRequest("항목5", 1000));
        wishlistService.add(new CreateWishlistItemRequest("항목6", 7000));
        return wishlistService.getAll();
    }
}
