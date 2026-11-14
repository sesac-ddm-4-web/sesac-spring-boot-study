package com.example.personal_project;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.http.MediaType;
import org.springframework.context.annotation.Import;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(properties = "spring.main.allow-bean-definition-overriding=true")
@Import(InMemoryRepositoryTestConfig.class)
@AutoConfigureMockMvc
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_EACH_TEST_METHOD)
class CollectionItemIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Test
    @DisplayName("아이템 생성 후 단건 및 전체 조회가 가능하다")
    void createAndFindItem() throws Exception {
        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"  키보드  ","price":50000}
                                """))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.APPLICATION_JSON))
                .andExpect(jsonPath("$.id").value(1))
                .andExpect(jsonPath("$.name").value("키보드"))
                .andExpect(jsonPath("$.price").value(50000));

        mockMvc.perform(get("/item/1"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("키보드"));

        mockMvc.perform(get("/item"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].id").value(1));
    }

    @Test
    @DisplayName("예산 이하의 아이템을 가격 오름차순으로 최대 세 개 추천한다")
    void recommendItemsWithinBudget() throws Exception {
        createItem("모니터", 200_000);
        createItem("키보드", 50_000);
        createItem("마우스", 30_000);
        createItem("장패드", 10_000);
        createItem("웹캠", 70_000);

        mockMvc.perform(get("/item/recommendations").param("budget", "100000"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(3)))
                .andExpect(jsonPath("$[0].name").value("장패드"))
                .andExpect(jsonPath("$[1].name").value("마우스"))
                .andExpect(jsonPath("$[2].name").value("키보드"));
    }

    @Test
    @DisplayName("필수 입력값이 없으면 400 응답을 반환한다")
    void rejectMissingRequiredFields() throws Exception {
        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(2)));
    }

    @Test
    @DisplayName("길이가 0인 이름은 400 응답을 반환한다")
    void rejectBlankName() throws Exception {
        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {"name":"","price":1000}
                                """))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)));
    }

    @Test
    @DisplayName("존재하지 않는 아이템을 조회하면 404 응답을 반환한다")
    void returnNotFoundForUnknownItem() throws Exception {
        mockMvc.perform(get("/item/999"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.errors[0]")
                        .value("해당 id로 item을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("음수 예산은 400 응답을 반환한다")
    void rejectNegativeBudget() throws Exception {
        mockMvc.perform(get("/item/recommendations").param("budget", "-1"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("예산은 0 이상입니다."));
    }

    @ParameterizedTest
    @ValueSource(strings = {" ", "   "})
    @DisplayName("공백만 있는 이름은 거부하고 저장하지 않는다")
    void rejectWhitespaceName(String name) throws Exception {
        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"price\":0}".formatted(name)))
                .andExpect(status().isBadRequest());
        mockMvc.perform(get("/item")).andExpect(content().json("[]"));
    }

    @Test
    @DisplayName("이름은 최대 40자까지 허용하고 음수 가격은 거부한다")
    void validateNameAndPriceBoundaries() throws Exception {
        createItem("가".repeat(40), 0);
        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"price\":0}".formatted("가".repeat(41))))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors", hasSize(1)));
        mockMvc.perform(post("/item").contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"상품\",\"price\":-1}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.errors[0]").value("price, 가격은 0이상입니다."));
        mockMvc.perform(get("/item")).andExpect(jsonPath("$", hasSize(1)));
    }

    @Test
    @DisplayName("빈 상품 목록과 추천 결과는 빈 배열이며 예산과 같은 가격도 추천한다")
    void emptyListsAndInclusiveBudget() throws Exception {
        mockMvc.perform(get("/item")).andExpect(status().isOk())
                .andExpect(content().json("[]"));
        mockMvc.perform(get("/item/recommendations").param("budget", "0"))
                .andExpect(status().isOk()).andExpect(content().json("[]"));
        createItem("무료 상품", 0);
        createItem("유료 상품", 1);
        mockMvc.perform(get("/item/recommendations").param("budget", "0"))
                .andExpect(status().isOk()).andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].price").value(0));
    }

    private void createItem(String name, int price) throws Exception {
        mockMvc.perform(post("/item")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"name\":\"%s\",\"price\":%d}".formatted(name, price)))
                .andExpect(status().isOk());
    }
}
