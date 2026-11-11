package com.example.demo;

import com.example.demo.support.FakeExternalProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.request.MockHttpServletRequestBuilder;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.web.context.WebApplicationContext;

import java.nio.charset.StandardCharsets;

import static com.example.demo.support.FakeExternalProductClient.product;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.put;

@SpringBootTest
@DirtiesContext(classMode = DirtiesContext.ClassMode.BEFORE_EACH_TEST_METHOD)
class ApiResponseTest {

    @TestConfiguration
    static class FakeClientConfig {
        @Bean
        @Primary
        FakeExternalProductClient fakeExternalProductClient() {
            FakeExternalProductClient client = new FakeExternalProductClient();
            client.add(product(1L, "Essence Mascara", "9.99", 99));
            client.add(product(2L, "Eyeshadow Palette", "19.99", 1));
            client.add(product(117L, "Sportbike Motorcycle", "7499.99", 0));
            return client;
        }
    }

    @Autowired
    private WebApplicationContext context;

    @Autowired
    private FakeExternalProductClient fakeClient;

    private MockMvc mockMvc;

    @BeforeEach
    void setUp() {
        mockMvc = MockMvcBuilders.webAppContextSetup(context).build();
    }

    private Response call(MockHttpServletRequestBuilder request) throws Exception {
        MvcResult result = mockMvc.perform(request).andReturn();
        return new Response(result.getResponse().getStatus(),
                result.getResponse().getContentAsString(StandardCharsets.UTF_8));
    }

    private Response json(MockHttpServletRequestBuilder request, String body) throws Exception {
        return call(request.contentType(MediaType.APPLICATION_JSON).content(body));
    }

    private record Response(int status, String body) {
    }

    @Test
    void 외부_상품_목록과_검색은_입력_조건을_어기면_400과_사유를_응답한다() throws Exception {
        Response overLimit = call(get("/external-products?limit=21"));
        Response negativeSkip = call(get("/external-products?skip=-1"));
        Response missingQuery = call(get("/external-products/search"));
        Response blankQuery = call(get("/external-products/search").param("q", " "));

        assertEquals(400, overLimit.status());
        assertTrue(overLimit.body().startsWith("limit: "));
        assertEquals(400, negativeSkip.status());
        assertTrue(negativeSkip.body().startsWith("skip: "));
        assertEquals(400, missingQuery.status());
        assertEquals("q: 필수 값입니다.", missingQuery.body());
        assertEquals(400, blankQuery.status());
        assertTrue(blankQuery.body().startsWith("q: "));
    }

    @Test
    void 외부_상품_검색_결과가_없으면_200과_빈_목록을_응답한다() throws Exception {
        Response response = call(get("/external-products/search?q=zzzz"));

        assertEquals(200, response.status());
        assertEquals("[]", response.body());
    }

    @Test
    void 외부_API가_실패하면_502를_응답한다() throws Exception {
        fakeClient.fail();

        assertEquals(502, call(get("/external-products")).status());
        assertEquals(502, call(get("/external-products/1")).status());
    }

    @Test
    void 외부_상품을_가져오면_환산한_가격과_외부_id로_저장된다() throws Exception {
        Response response = json(post("/collectionItems/import"), "{\"externalId\":1}");

        assertEquals(200, response.status());
        assertTrue(response.body().contains("\"price\":9990"));
        assertTrue(response.body().contains("\"sourceType\":\"EXTERNAL\""));
        assertTrue(response.body().contains("\"externalId\":1"));
    }

    @Test
    void 가져오기에_실패하면_상황별_상태_코드를_응답하고_목록은_그대로다() throws Exception {
        json(post("/collectionItems/import"), "{\"externalId\":1}");

        assertEquals(400, json(post("/collectionItems/import"), "{}").status());
        assertEquals(400, json(post("/collectionItems/import"), "{\"externalId\":0}").status());
        assertEquals(404, json(post("/collectionItems/import"), "{\"externalId\":99999}").status());
        assertEquals(409, json(post("/collectionItems/import"), "{\"externalId\":1}").status());
        fakeClient.fail();
        assertEquals(502, json(post("/collectionItems/import"), "{\"externalId\":2}").status());

        Response list = call(get("/collectionItems"));
        assertEquals(1, list.body().split("\"id\":").length - 1);
    }

    @Test
    void 가져온_외부_상품을_수정하면_409를_응답한다() throws Exception {
        json(post("/collectionItems/import"), "{\"externalId\":1}");

        Response response = json(put("/collectionItems/1"), "{\"name\":\"변경\",\"price\":1}");

        assertEquals(409, response.status());
        assertTrue(call(get("/collectionItems/1")).body().contains("\"price\":9990"));
    }

    @Test
    void 위시리스트_입력이_잘못되면_400과_필드별_사유를_응답한다() throws Exception {
        Response response = json(post("/collectionItems"), "{\"name\":\" \",\"price\":-1}");

        assertEquals(400, response.status());
        assertTrue(response.body().contains("name: "));
        assertTrue(response.body().contains("price: "));
    }

    @Test
    void 요청_본문_형식이_잘못되면_400을_응답한다() throws Exception {
        Response response = json(post("/cart/items"), "{\"itemId\":");

        assertEquals(400, response.status());
        assertEquals("요청 본문 형식이 올바르지 않습니다.", response.body());
    }

    @Test
    void 장바구니_담기에_실패하면_상황별_상태_코드를_응답한다() throws Exception {
        json(post("/collectionItems"), "{\"name\":\"코트\",\"price\":110000}");
        json(post("/collectionItems/import"), "{\"externalId\":117}");

        Response overQuantity = json(post("/cart/items"), "{\"itemId\":1,\"quantity\":6}");
        assertEquals(400, overQuantity.status());
        assertTrue(overQuantity.body().startsWith("quantity: "));
        assertEquals(404, json(post("/cart/items"), "{\"itemId\":99,\"quantity\":1}").status());
        assertEquals(409, json(post("/cart/items"), "{\"itemId\":2,\"quantity\":1}").status());
        assertEquals(200, json(post("/cart/items"), "{\"itemId\":1,\"quantity\":3}").status());
        assertEquals(409, json(post("/cart/items"), "{\"itemId\":1,\"quantity\":3}").status());
        assertEquals(404, json(patch("/cart/items/99"), "{\"quantity\":1}").status());

        assertTrue(call(get("/cart")).body().contains("\"quantity\":3"));
    }

    @Test
    void 금액_미리보기는_쿠폰별_금액을_응답하고_잘못된_쿠폰은_400이다() throws Exception {
        assertEquals(400, call(get("/cart/preview?couponCode=PERCENT10")).status());

        json(post("/collectionItems"), "{\"name\":\"코트\",\"price\":110000}");
        json(post("/cart/items"), "{\"itemId\":1,\"quantity\":1}");

        assertTrue(call(get("/cart/preview")).body().contains("\"finalAmount\":110000"));
        assertTrue(call(get("/cart/preview?couponCode=PERCENT10")).body().contains("\"finalAmount\":104000"));
        assertTrue(call(get("/cart/preview?couponCode=FIXED15000")).body().contains("\"finalAmount\":100000"));
        assertEquals(400, call(get("/cart/preview?couponCode=ABC")).status());
    }
}
