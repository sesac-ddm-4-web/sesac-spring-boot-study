package com.example.wishlist.management.infrastructure.external;

import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.exception.ExternalApiException;
import com.example.wishlist.management.domain.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.math.BigDecimal;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class DummyJsonProductClientTest {
    private MockRestServiceServer server;
    private DummyJsonProductClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dummyjson.com");
        server = MockRestServiceServer.bindTo(builder).build();   // 이 빌더로 만든 RestClient의 요청을 가로챔
        client = new DummyJsonProductClient(builder.build());
    }

    @Test
    void mapsJson_andIgnoresUnknownFields() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess("""
                        {"id":1,"title":"Mascara","price":9.99,"category":"beauty",
                         "thumbnail":"t.png","stock":30,"rating":4.9,"tags":["a","b"]}
                        """, MediaType.APPLICATION_JSON));

        ExternalProduct product = client.get(1);

        assertEquals("Mascara", product.title());
        assertEquals(0, new BigDecimal("9.99").compareTo(product.price()));
        assertEquals(30, product.stock());
    }

    @Test
    void external404_becomesNotFoundException() {
        server.expect(requestTo("https://dummyjson.com/products/999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(NotFoundException.class, () -> client.get(999));
    }

    @Test
    void external500_becomesExternalApiException() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withServerError());
        assertThrows(ExternalApiException.class, () -> client.get(1));
    }

    @Test
    void emptyBody_becomesExternalApiException() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withSuccess());
        assertThrows(ExternalApiException.class, () -> client.get(1));
    }
}