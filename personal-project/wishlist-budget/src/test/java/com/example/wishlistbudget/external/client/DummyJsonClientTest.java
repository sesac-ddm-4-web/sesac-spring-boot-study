package com.example.wishlistbudget.external.client;

import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.exception.ExternalApiException;
import com.example.wishlistbudget.exception.ExternalProductNotFoundException;

import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.*;

class DummyJsonClientTest {

    private HttpServer server;
    private DummyJsonClient client;

    @BeforeEach
    void setUp() throws IOException {

        server = HttpServer.create(
                new InetSocketAddress("127.0.0.1", 0),
                0
        );

        server.createContext("/products/1", exchange -> {

            String json = """
                    {
                        "id": 1,
                        "title": "운동화",
                        "price": 50.00,
                        "category": "shoes",
                        "thumbnail": "thumbnail.png",
                        "stock": 10
                    }
                    """;

            byte[] body = json.getBytes(StandardCharsets.UTF_8);

            exchange.getResponseHeaders().set(
                    "Content-Type", "application/json"
            );

            exchange.sendResponseHeaders(200, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/products/999", exchange -> {

            byte[] body = """
                    {"message":"Product not found"}
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(404, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.createContext("/products/500", exchange -> {

            byte[] body = """
                    {"message":"Internal Server Error"}
                    """.getBytes(StandardCharsets.UTF_8);

            exchange.sendResponseHeaders(500, body.length);

            try (var output = exchange.getResponseBody()) {
                output.write(body);
            }
        });

        server.start();

        int port = server.getAddress().getPort();

        RestClient restClient = RestClient.builder()
                .baseUrl("http://127.0.0.1:" + port)
                .build();

        client = new DummyJsonClient(restClient);
    }

    @AfterEach
    void tearDown() {

        if (server != null) {
            server.stop(0);
        }
    }

    @Test
    @DisplayName("외부 API에서 상품을 정상 조회한다")
    void getProduct_success() {

        DummyJsonProduct result = client.getProduct(1L);

        assertEquals(1L, result.id());
        assertEquals("운동화", result.title());
        assertEquals(0, result.price().compareTo(
                new java.math.BigDecimal("50.00")
        ));
        assertEquals(10, result.stock());
    }

    @Test
    @DisplayName("외부 API가 404를 반환하면 상품 없음 예외가 발생한다")
    void getProduct_notFound() {

        assertThrows(
                ExternalProductNotFoundException.class,
                () -> client.getProduct(999L)
        );
    }

    @Test
    @DisplayName("외부 API가 500을 반환하면 외부 API 예외가 발생한다")
    void getProduct_serverError() {

        assertThrows(
                ExternalApiException.class,
                () -> client.getProduct(500L)
        );
    }
}