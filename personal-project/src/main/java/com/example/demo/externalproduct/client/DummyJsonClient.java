package com.example.demo.externalproduct.client;

import com.example.demo.externalproduct.client.dto.DummyJsonProduct;
import com.example.demo.externalproduct.client.dto.DummyJsonProductList;
import com.example.demo.global.exception.EntityNotFoundException;
import com.example.demo.global.exception.ExternalApiException;
import org.springframework.http.client.SimpleClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.time.Duration;
import java.util.List;

// ExternalProductClient의 실제 구현체
@Component
public class DummyJsonClient implements ExternalProductClient {

    private static final String BASE_URL = "https://dummyjson.com";

    private final RestClient restClient;

    public DummyJsonClient() {
        // 제한 시간 설정
        SimpleClientHttpRequestFactory factory = new SimpleClientHttpRequestFactory();
        factory.setConnectTimeout(Duration.ofSeconds(3)); // 연결까지 최대 3초
        factory.setReadTimeout(Duration.ofSeconds(5));    // 연결 후 응답까지 최대 5초

        this.restClient = RestClient.builder()
                .baseUrl(BASE_URL)
                .requestFactory(factory)
                .build();
    }

    @Override
    public List<DummyJsonProduct> findAll(int limit, int skip) {
        try {
            DummyJsonProductList body = restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class); // 실제 요청 후 JSON → DTO 변환
            return body.products();
        } catch (RestClientException e) {
            // 연결 실패, 시간 초과, 외부 5xx 등
            throw new ExternalApiException("외부 상품 API 호출에 실패했습니다.", e);
        }
    }

    @Override
    public List<DummyJsonProduct> search(String q, int limit, int skip) {
        try {
            DummyJsonProductList body = restClient.get()
                    .uri("/products/search?q={q}&limit={limit}&skip={skip}", q, limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);
            return body.products();
        } catch (RestClientException e) {
            throw new ExternalApiException("외부 상품 API 호출에 실패했습니다.", e);
        }
    }

    @Override
    public DummyJsonProduct findById(long id) {
        try {
            return restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProduct.class);
        } catch (HttpClientErrorException.NotFound e) {
            throw new EntityNotFoundException("외부 상품을 찾지 못했습니다."); // NotFound는 RestClientException의 하위 타입이라 아래보다 먼저 써야함
        } catch (RestClientException e) {
            throw new ExternalApiException("외부 상품 API 호출에 실패했습니다.", e);
        }
    }
}
