package com.example.sesac_spring_boot_study.product;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.List;

import com.example.sesac_spring_boot_study.domain.Product;
import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

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

    // Tests can supply a client connected to a local test server.
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

    public DummyJsonProductList list(int limit, int skip) {
        try {
            DummyJsonProductList product = restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);

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

    public DummyJsonProductList search(String q) {
        try {
            DummyJsonProductList product = restClient.get()
                    .uri("/products/search?q={q}", q)
                    .retrieve()
                    .body(DummyJsonProductList.class);

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

}
