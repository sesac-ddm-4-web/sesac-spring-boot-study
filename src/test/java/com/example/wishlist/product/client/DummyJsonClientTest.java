package com.example.wishlist.product.client;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withResourceNotFound;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withServerError;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;
import com.example.wishlist.product.exception.ExternalApiException;
import com.example.wishlist.product.exception.ExternalProductNotFoundException;
import java.math.BigDecimal;
import java.net.SocketTimeoutException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

public class DummyJsonClientTest {

    private static final String PRODUCT_1 = """
            {
              "id": 1,
              "title": "Essence Mascara Lash Princess",
              "price": 9.99,
              "category": "beauty",
              "thumbnail": "https://cdn.dummyjson.com/product-images/beauty/essence-mascara-lash-princess/thumbnail.webp",
              "stock": 99,
              "discountPercentage": 10.48,
              "minimumOrderQuantity": 48
            }
            """;

    private MockRestServiceServer server;
    private DummyJsonClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dummyjson.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DummyJsonClient(builder.build());
    }

    @Test
    @DisplayName("상세 조회: 필요한 필드만 읽고, 사용하지 않는 필드(discountPercentage 등)는 무시한다")
    void findById() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
            .andExpect(method(HttpMethod.GET))
            .andRespond(withSuccess(PRODUCT_1, MediaType.APPLICATION_JSON));

        ExternalProduct product = client.findById(1L);

        assertThat(product.id()).isEqualTo(1L);
        assertThat(product.title()).isEqualTo("Essence Mascara Lash Princess");
        assertThat(product.price()).isEqualByComparingTo(new BigDecimal("9.99"));
        assertThat(product.category()).isEqualTo("beauty");
        assertThat(product.stock()).isEqualTo(99);
        assertThat(product.priceInWon()).isEqualTo(9990);
        server.verify();
    }

    @Test
    @DisplayName("외부 서버가 404면 ExternalProductNotFoundException (→ 404)")
    void notFound() {
        server.expect(requestTo("https://dummyjson.com/products/9999"))
            .andRespond(withResourceNotFound());

        assertThatThrownBy(() -> client.findById(9999L))
            .isInstanceOf(ExternalProductNotFoundException.class);
    }

    @Test
    @DisplayName("외부 서버 오류(500)면 ExternalApiException (→ 502)")
    void serverError() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
            .andRespond(withServerError());

        assertThatThrownBy(() -> client.findById(1L))
            .isInstanceOf(ExternalApiException.class);
    }

    @Test
    @DisplayName("연결 실패·시간 초과면 ExternalApiException (→ 502)")
    void timeout() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
            .andRespond(request -> {
                throw new SocketTimeoutException("Read timed out");
            });

        assertThatThrownBy(() -> client.findById(1L))
            .isInstanceOf(ExternalApiException.class);
    }

    @Test
    @DisplayName("목록 조회: limit·skip을 그대로 전달하고 total 등을 읽는다")
    void findAll() {
        server.expect(requestTo("https://dummyjson.com/products?limit=10&skip=0"))
            .andRespond(withSuccess("""
                        {"products": [%s], "total": 194, "skip": 0, "limit": 10}
                        """.formatted(PRODUCT_1), MediaType.APPLICATION_JSON));

        ExternalProductPage page = client.findAll(10, 0);

        assertThat(page.products()).hasSize(1);
        assertThat(page.total()).isEqualTo(194);
        assertThat(page.limit()).isEqualTo(10);
    }

    @Test
    @DisplayName("검색 결과가 없으면 예외 없이 빈 목록")
    void searchEmpty() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=zzzz&limit=10&skip=0"))
            .andRespond(withSuccess("""
                        {"products": [], "total": 0, "skip": 0, "limit": 0}
                        """, MediaType.APPLICATION_JSON));

        ExternalProductPage page = client.search("zzzz", 10, 0);

        assertThat(page.products()).isEmpty();
        assertThat(page.total()).isZero();
    }
}
