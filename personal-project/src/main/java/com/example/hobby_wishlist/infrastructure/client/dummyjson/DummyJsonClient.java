package com.example.hobby_wishlist.infrastructure.client.dummyjson;

import com.example.hobby_wishlist.domain.externalproduct.ExternalProduct;
import com.example.hobby_wishlist.domain.externalproduct.ExternalProductProvider;
import com.example.hobby_wishlist.infrastructure.client.dummyjson.model.DummyJsonProduct;
import com.example.hobby_wishlist.infrastructure.client.dummyjson.model.DummyJsonProductPage;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.server.ResponseStatusException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

@Component
public class DummyJsonClient implements ExternalProductProvider {
    private static final String REQUEST_URL = "https://dummyjson.com";
    private final RestClient restClient;

    public DummyJsonClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();
        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
                .baseUrl(REQUEST_URL)
                .requestFactory(factory)
                .build();
    }

    @Override
    public ExternalProduct findById(Long id) {
        try {
            DummyJsonProduct response = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProduct.class);

            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 응답이 비어 있습니다.");
            }
            return response.toExternalProduct();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "외부 상품을 찾을 수 없습니다.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 API 호출이 실패했습니다.");
        }
    }

    @Override
    public List<ExternalProduct> findAll(Integer limit, Integer skip) {
        try {
            DummyJsonProductPage response = restClient.get()
                    .uri("products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductPage.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 응답이 비어 있습니다.");
            }
            return response.products();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "외부 상품을 찾을 수 없습니다.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 API 호출이 실패했습니다.");
        }
    }

    @Override
    public List<ExternalProduct> search(String query, Integer limit, Integer skip) {
        try {
            DummyJsonProductPage response = restClient.get()
                    .uri(builder -> builder
                            .path("/products/search")
                            .queryParam("q", "{query}")
                            .queryParam("limit", limit)
                            .queryParam("skip", skip)
                            .build(query))
                    .retrieve()
                    .body(DummyJsonProductPage.class);
            if (response == null) {
                throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 응답이 비어 있습니다.");
            }
            return response.products();
        } catch (HttpClientErrorException.NotFound e) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "외부 상품을 찾을 수 없습니다.");
        } catch (RestClientException e) {
            throw new ResponseStatusException(HttpStatus.BAD_GATEWAY, "외부 상품 API 호출이 실패했습니다.");
        }
    }
}
