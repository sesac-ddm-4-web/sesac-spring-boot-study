
package com.example.wishlistbudget;

import com.example.wishlistbudget.external.client.DummyJsonClient;
import com.sun.net.httpserver.HttpServer;

import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.context.TestConfiguration;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Primary;
import org.springframework.http.MediaType;
import org.springframework.test.context.TestPropertySource;
import org.springframework.web.client.RestClient;

import java.io.IOException;
import java.net.InetSocketAddress;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;

import static org.junit.jupiter.api.Assertions.assertEquals;

@SpringBootTest(
        webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT
)
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration_test;DB_CLOSE_DELAY=-1"
})
class ExternalApiIntegrationTest {

    @Value("${local.server.port}")
    private int port;

    private final HttpClient httpClient = HttpClient.newHttpClient();

    // 실제 DummyJSON 대신 사용하는 테스트 서버
    private static final HttpServer fakeServer = createFakeServer();

    private static HttpServer createFakeServer() {

        try {
            HttpServer server = HttpServer.create(
                    new InetSocketAddress("127.0.0.1", 0),
                    0
            );

            server.createContext("/products/", exchange -> {

                String path = exchange.getRequestURI().getPath();

                int status;
                String json;

                if (path.equals("/products/1")) {

                    status = 200;

                    json = """
                            {
                                "id": 1,
                                "title": "운동화",
                                "price": 50.00,
                                "category": "shoes",
                                "thumbnail": "thumbnail.png",
                                "stock": 10
                            }
                            """;

                } else if (path.equals("/products/2")) {

                    status = 200;

                    json = """
                            {
                                "id": 2,
                                "title": "운동복",
                                "price": 30.00,
                                "category": "clothes",
                                "thumbnail": "thumbnail.png",
                                "stock": 5
                            }
                            """;

                } else if (path.equals("/products/500")) {

                    status = 500;

                    json = """
                            {
                                "message": "Server Error"
                            }
                            """;

                } else {

                    status = 404;

                    json = """
                            {
                                "message": "Product not found"
                            }
                            """;
                }

                byte[] body = json.getBytes(StandardCharsets.UTF_8);

                exchange.getResponseHeaders().set(
                        "Content-Type",
                        "application/json"
                );

                exchange.sendResponseHeaders(status, body.length);

                try (var output = exchange.getResponseBody()) {
                    output.write(body);
                }
            });

            server.start();

            return server;

        } catch (IOException e) {

            throw new IllegalStateException(
                    "테스트 서버 실행 실패", e
            );
        }
    }

    @TestConfiguration
    static class TestConfig {

        @Bean
        @Primary
        DummyJsonClient testDummyJsonClient() {

            int port = fakeServer.getAddress().getPort();

            RestClient restClient = RestClient.builder()
                    .baseUrl("http://127.0.0.1:" + port)
                    .build();

            return new DummyJsonClient(restClient);
        }
    }

    @AfterAll
    static void tearDown() {

        fakeServer.stop(0);
    }

    // 외부 상품 등록 POST 요청
    private HttpResponse<String> postImport(Long externalId)
            throws Exception {

        String json = """
                {"externalId": %d}
                """.formatted(externalId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port + "/collections/import"
                ))
                .header(
                        "Content-Type",
                        MediaType.APPLICATION_JSON_VALUE
                )
                .POST(HttpRequest.BodyPublishers.ofString(json))
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    // 외부 상품 조회 GET 요청
    private HttpResponse<String> getProduct(Long id)
            throws Exception {

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(
                        "http://localhost:" + port
                                + "/external/products/" + id
                ))
                .GET()
                .build();

        return httpClient.send(
                request,
                HttpResponse.BodyHandlers.ofString()
        );
    }

    @Test
    @DisplayName("외부 상품 등록에 성공하면 HTTP 201을 반환한다")
    void importProduct_success() throws Exception {

        HttpResponse<String> response = postImport(1L);

        assertEquals(201, response.statusCode());
    }

    @Test
    @DisplayName("외부 상품 ID가 0이면 HTTP 400을 반환한다")
    void importProduct_invalidId() throws Exception {

        HttpResponse<String> response = postImport(0L);

        assertEquals(400, response.statusCode());
    }

    @Test
    @DisplayName("존재하지 않는 외부 상품 등록 시 HTTP 404를 반환한다")
    void importProduct_notFound() throws Exception {

        HttpResponse<String> response = postImport(999L);

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("이미 등록된 외부 상품은 HTTP 409를 반환한다")
    void importProduct_duplicate() throws Exception {

        HttpResponse<String> first = postImport(2L);
        HttpResponse<String> second = postImport(2L);

        assertEquals(201, first.statusCode());
        assertEquals(409, second.statusCode());
    }

    @Test
    @DisplayName("외부 서버가 500을 반환하면 등록 API는 HTTP 502를 반환한다")
    void importProduct_badGateway() throws Exception {

        HttpResponse<String> response = postImport(500L);

        assertEquals(502, response.statusCode());
    }

    @Test
    @DisplayName("존재하지 않는 외부 상품 조회 시 HTTP 404를 반환한다")
    void getProduct_notFound() throws Exception {

        HttpResponse<String> response = getProduct(999L);

        assertEquals(404, response.statusCode());
    }

    @Test
    @DisplayName("외부 서버가 500을 반환하면 조회 API는 HTTP 502를 반환한다")
    void getProduct_badGateway() throws Exception {

        HttpResponse<String> response = getProduct(500L);

        assertEquals(502, response.statusCode());
    }
}
