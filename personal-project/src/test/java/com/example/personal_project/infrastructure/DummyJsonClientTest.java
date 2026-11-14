package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.exception.CommonBadGateWayException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;

class DummyJsonClientTest {
    private MockRestServiceServer server;
    private DummyJsonClient client;

    @BeforeEach
    void setUp() {
        RestClient.Builder builder = RestClient.builder().baseUrl("https://dummyjson.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DummyJsonClient(builder.build());
    }

    @AfterEach
    void verifyRequests() {
        server.verify();
    }

    @ParameterizedTest
    @ValueSource(strings = {"single", "list", "search"})
    @DisplayName("외부 응답 본문이 없으면 연동 실패 예외를 던진다")
    void rejectEmptyBody(String operation) {
        server.expect(requestTo(url(operation))).andRespond(withSuccess());
        assertThrows(CommonBadGateWayException.class, () -> call(operation));
    }

    @ParameterizedTest
    @ValueSource(strings = {"list", "search"})
    @DisplayName("products가 null이면 실패하지만 빈 배열이면 정상 결과로 반환한다")
    void distinguishNullAndEmptyProducts(String operation) {
        server.expect(requestTo(url(operation)))
                .andRespond(withSuccess("{\"products\":null}", MediaType.APPLICATION_JSON));
        server.expect(requestTo(url(operation)))
                .andRespond(withSuccess("{\"products\":[],\"total\":0,\"limit\":10,\"skip\":0}",
                        MediaType.APPLICATION_JSON));
        assertThrows(CommonBadGateWayException.class, () -> call(operation));
        var result = operation.equals("list") ? client.getProducts(10, 0) : client.searchProducts("phone", 10, 0);
        assertTrue(result.products().isEmpty());
        assertEquals(0, result.total());
    }

    @ParameterizedTest
    @ValueSource(strings = {"single", "list", "search"})
    @DisplayName("외부 통신 예외는 연동 실패 예외로 변환한다")
    void translateIoFailure(String operation) {
        server.expect(requestTo(url(operation)))
                .andRespond(withException(new IOException("simulated read timeout")));
        assertThrows(CommonBadGateWayException.class, () -> call(operation));
    }

    private Object call(String operation) {
        return switch (operation) {
            case "single" -> client.findById(42L);
            case "list" -> client.getProducts(10, 0);
            case "search" -> client.searchProducts("phone", 10, 0);
            default -> throw new IllegalArgumentException(operation);
        };
    }

    private String url(String operation) {
        return "https://dummyjson.com" + switch (operation) {
            case "single" -> "/products/42";
            case "list" -> "/products?limit=10&skip=0";
            case "search" -> "/products/search?q=phone&limit=10&skip=0";
            default -> throw new IllegalArgumentException(operation);
        };
    }
}
