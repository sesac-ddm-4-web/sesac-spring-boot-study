package com.example.wishlist.product.client;

import com.example.wishlist.product.domain.ExternalProduct;
import com.example.wishlist.product.domain.ExternalProductPage;
import com.example.wishlist.product.exception.ExternalApiException;
import com.example.wishlist.product.exception.ExternalProductNotFoundException;
import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

// DummyJSON 상품 API RestClient로 호출
@Component
public class DummyJsonClient implements ProductClient {

    private static final String BASE_URL = "https://dummyjson.com";

    private final RestClient restClient;

    public DummyJsonClient() {
        HttpClient httpClient = HttpClient.newBuilder()
            .connectTimeout(Duration.ofSeconds(3))
            .build();

        JdkClientHttpRequestFactory factory = new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        this.restClient = RestClient.builder()
            .baseUrl(BASE_URL)
            .requestFactory(factory)
            .build();
    }

    DummyJsonClient (RestClient restClient) {
        this.restClient = restClient;
    }

    @Override
    public ExternalProductPage findAll(int limit, int skip) {
        DummyJsonProductPage page = fetch(
            () -> restClient.get()
                .uri("/products?limit={limit}&skip={skip}", limit, skip)
                .retrieve()
                .body(DummyJsonProductPage.class),
            () -> new ExternalApiException("외부 상품 목록 주소를 찾을 수 없습니다. (HTTP 404)"));
        return page.toExternalProductPage();
    }

    @Override
    public ExternalProductPage search(String keyword, int limit, int skip) {
        DummyJsonProductPage page = fetch(
            () -> restClient.get()
                .uri("/products/search?q={q}&limit={limit}&skip={skip}", keyword, limit, skip)
                .retrieve()
                .body(DummyJsonProductPage.class),
            () -> new ExternalApiException("외부 상품 검색 주소를 찾을 수 없습니다. (HTTP 404)"));
        return page.toExternalProductPage();
    }

    @Override
    public ExternalProduct findById(long id) {
        DummyJsonProduct product = fetch(
            () -> restClient.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(DummyJsonProduct.class),
            () -> new ExternalProductNotFoundException(id));
        return product.toExternalProduct();
    }

    // 공통 호출 처리
    private <T> T fetch(Supplier<T> request, Supplier<RuntimeException> whenNotFound) {
        T body;
        try {
            body = request.get();
        } catch (RestClientResponseException exception) {
            if (exception.getStatusCode().value() == 404) {
                throw whenNotFound.get();
            }
            throw new ExternalApiException(
                "외부 상품 서버가 오류를 반환했습니다. (HTTP " + exception.getStatusCode().value() + ")");
        } catch (RestClientException exception) {
            throw new ExternalApiException("외부 상품 정보를 가져오지 못했습니다. (연결 실패 또는 시간 초과)");
        }
        if (body == null) {
            throw new ExternalApiException("외부 상품 응답이 비어 있습니다.");
        }
        return body;
    }
}
