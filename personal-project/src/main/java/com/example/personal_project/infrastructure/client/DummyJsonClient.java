package com.example.personal_project.infrastructure.client;

import com.example.personal_project.application.port.ProductClient;
import com.example.personal_project.domain.product.model.ExternalProduct;
import com.example.personal_project.domain.product.model.ExternalProductPage;
import com.example.personal_project.infrastructure.client.dto.DummyJsonProduct;
import com.example.personal_project.infrastructure.client.dto.DummyJsonProductsResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import com.example.personal_project.global.exception.ExternalApiException;
import com.example.personal_project.global.exception.ExternalProductNotFoundException;
import java.util.function.Supplier;
import org.springframework.web.client.RestClient;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class DummyJsonClient implements ProductClient {

    private final RestClient restClient;

    @Autowired
    public DummyJsonClient(RestClient.Builder builder) {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = builder
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }

    DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    private <T> T execute(Supplier<T> request, Long externalId) {
        try {
            return request.get();
        } catch (RestClientResponseException exception) {
            if (externalId != null && exception.getStatusCode().value() == 404) {
                throw new ExternalProductNotFoundException(externalId);
            }
            throw new ExternalApiException("외부 API HTTP 오류", exception);
        } catch (RestClientException exception) {
            throw new ExternalApiException("외부 API 연결 또는 응답 변환 실패", exception);
        }
    }

    @Override
    public ExternalProductPage findAll(int limit, int skip) {
        DummyJsonProductsResponse response = execute(() -> restClient.get()
                .uri(builder -> builder
                        .path("/products")
                        .queryParam("limit", limit)
                        .queryParam("skip", skip)
                        .build())
                .retrieve()
                .body(DummyJsonProductsResponse.class), null);
        return toDomain(response);
    }

    @Override
    public ExternalProductPage search(String query, int limit, int skip) {
        DummyJsonProductsResponse response = execute(() -> restClient.get()
                .uri(builder -> builder
                        .path("/products/search")
                        .queryParam("q", "{query}")
                        .queryParam("limit", limit)
                        .queryParam("skip", skip)
                        .build(query))
                .retrieve()
                .body(DummyJsonProductsResponse.class), null);
        return toDomain(response);
    }

    @Override
    public ExternalProduct findById(Long externalId) {
        DummyJsonProduct response = execute(() -> restClient.get()
                .uri("/products/{id}", externalId)
                .retrieve()
                .body(DummyJsonProduct.class), externalId);
        return toDomain(response);
    }

    private ExternalProduct toDomain(DummyJsonProduct response) {
        if (response == null
                || response.id() == null
                || response.title() == null
                || response.price() == null
                || response.category() == null
                || response.thumbnail() == null
                || response.stock() == null
                || response.id() <= 0 || response.price().signum() < 0 || response.stock() < 0) {
            throw new ExternalApiException("외부 상품 응답에 필수 값이 없습니다.");
        }
        return new ExternalProduct(
                response.id(),
                response.title(),
                response.price(),
                response.category(),
                response.thumbnail(),
                response.stock()
        );
    }

    private ExternalProductPage toDomain(DummyJsonProductsResponse response) {
        if (response == null
                || response.products() == null
                || response.total() == null
                || response.skip() == null
                || response.limit() == null
                || response.total() < 0 || response.skip() < 0 || response.limit() < 0) {
            throw new ExternalApiException("외부 목록 응답에 필수 값이 없습니다.");
        }

        return new ExternalProductPage(
                response.products().stream()
                        .map(this::toDomain)
                        .toList(),
                response.total(),
                response.skip(),
                response.limit()
        );
    }
}
