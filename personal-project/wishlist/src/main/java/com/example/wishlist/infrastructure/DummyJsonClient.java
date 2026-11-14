package com.example.wishlist.infrastructure;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class DummyJsonClient {
    private final RestClient restClient;

    public DummyJsonClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }

    DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public DummyJsonProduct findById(long id) {
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST, "상품 id는 양수여야 합니다");
        }

        try {
            DummyJsonProduct product = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProduct.class);

            if (product == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY, "외부 상품 응답이 비어 있습니다");
            }
            return product;
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "외부 상품을 찾을 수 없습니다");
            }
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 상품 서버가 오류를 반환했습니다");
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 상품 정보를 가져오지 못했습니다");
        }
    }

    public List<DummyJsonProduct> findAll(int limit, int skip) {
        try {
            DummyJsonProductList result = restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);
            return productsOf(result);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 상품 목록을 가져오지 못했습니다");
        }
    }

    public List<DummyJsonProduct> search(String q, int limit, int skip) {
        try {
            DummyJsonProductList result = restClient.get()
                    .uri("/products/search?q={q}&limit={limit}&skip={skip}", q, limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);
            return productsOf(result);
        } catch (RestClientException exception) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY, "외부 상품 검색 결과를 가져오지 못했습니다");
        }
    }

    private List<DummyJsonProduct> productsOf(DummyJsonProductList result) {
        if (result == null || result.products() == null) {
            return List.of();
        }
        return result.products();
    }
}
