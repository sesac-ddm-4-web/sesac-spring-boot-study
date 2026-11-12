package com.example.personal_project.infrastructure.client;

import com.example.personal_project.global.exception.*;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import java.net.SocketTimeoutException;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.*;
import static org.springframework.test.web.client.response.MockRestResponseCreators.*;
import static org.junit.jupiter.api.Assertions.*;

class DummyJsonClientTest {
    private MockRestServiceServer server;
    private DummyJsonClient client;
    @BeforeEach void setUp() {
        var builder = RestClient.builder().baseUrl("https://dummyjson.com");
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DummyJsonClient(builder.build());
    }
    @Test void detail404AndOtherHttpErrorsAreSeparated() {
        server.expect(requestTo("https://dummyjson.com/products/1")).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(ExternalProductNotFoundException.class, () -> client.findById(1L));
        server.verify();
    }
    @Test void upstream500BecomesGatewayFailure() {
        server.expect(anything()).andRespond(withServerError());
        assertThrows(ExternalApiException.class, () -> client.findById(1L));
    }
    @Test void timeoutPreservesCause() {
        server.expect(anything()).andRespond(withException(new SocketTimeoutException("timeout")));
        var exception = assertThrows(ExternalApiException.class, () -> client.findById(1L));
        assertNotNull(exception.getCause());
    }
    @Test void malformedJsonIsGatewayFailure() {
        server.expect(anything()).andRespond(withSuccess("invalid-json", MediaType.APPLICATION_JSON));
        assertThrows(ExternalApiException.class, () -> client.findById(1L));
    }
    @Test void missingFieldsAndNullProductsAreGatewayFailure() {
        server.expect(anything()).andRespond(withSuccess("{}", MediaType.APPLICATION_JSON));
        assertThrows(ExternalApiException.class, () -> client.findById(1L));
    }
    @Test void listAndSearchReturnEmptyPages() {
        server.expect(requestTo("https://dummyjson.com/products?limit=10&skip=0"))
                .andRespond(withSuccess("{\"products\":[],\"total\":0,\"skip\":0,\"limit\":10}", MediaType.APPLICATION_JSON));
        assertTrue(client.findAll(10,0).products().isEmpty());
        server.verify();
    }
    @Test void searchForwardsPageParameters() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone&limit=5&skip=2"))
                .andRespond(withSuccess("{\"products\":[],\"total\":0,\"skip\":2,\"limit\":5}", MediaType.APPLICATION_JSON));
        assertEquals(2,client.search("phone",5,2).skip());
    }

    @Test void validProductIgnoresUnusedFieldsAndKeepsDecimalPrice() {
        server.expect(anything()).andRespond(withSuccess("""
            {"id":42,"title":"상품","price":9.99,"category":"beauty","thumbnail":"image",
             "stock":0,"discountPercentage":99,"minimumOrderQuantity":100}
            """,MediaType.APPLICATION_JSON));
        var product=client.findById(42L);
        assertEquals(42L,product.externalId());
        assertEquals(new java.math.BigDecimal("9.99"),product.price());
        assertEquals(0,product.stock());
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={
        "{}", "{\"products\":null,\"total\":0,\"skip\":0,\"limit\":10}",
        "{\"products\":[null],\"total\":1,\"skip\":0,\"limit\":10}",
        "{\"products\":[],\"total\":-1,\"skip\":0,\"limit\":10}",
        "{\"products\":[],\"total\":0,\"skip\":-1,\"limit\":10}",
        "{\"products\":[],\"total\":0,\"skip\":0,\"limit\":-1}"
    })
    void invalidPageResponsesBecomeGatewayFailures(String json) {
        server.expect(anything()).andRespond(withSuccess(json,MediaType.APPLICATION_JSON));
        assertThrows(ExternalApiException.class,()->client.findAll(10,0));
    }

    @org.junit.jupiter.params.ParameterizedTest
    @org.junit.jupiter.params.provider.ValueSource(strings={
        "{\"id\":0,\"title\":\"x\",\"price\":1,\"category\":\"c\",\"thumbnail\":\"i\",\"stock\":1}",
        "{\"id\":1,\"title\":\"x\",\"price\":-1,\"category\":\"c\",\"thumbnail\":\"i\",\"stock\":1}",
        "{\"id\":1,\"title\":\"x\",\"price\":1,\"category\":\"c\",\"thumbnail\":\"i\",\"stock\":-1}"
    })
    void invalidProductValuesBecomeGatewayFailures(String json) {
        server.expect(anything()).andRespond(withSuccess(json,MediaType.APPLICATION_JSON));
        assertThrows(ExternalApiException.class,()->client.findById(1L));
    }

    @Test void list404IsGatewayFailureAndEmptyBodyIsRejected() {
        server.expect(anything()).andRespond(withStatus(HttpStatus.NOT_FOUND));
        assertThrows(ExternalApiException.class,()->client.findAll(10,0));
    }
}
