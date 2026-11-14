
package com.example.campkit.external;

import com.example.campkit.exception.ExternalApiException;
import com.example.campkit.exception.NotFoundException;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.ResourceAccessException;

import java.net.http.HttpClient;
import java.time.Duration;
import java.util.function.Supplier;

@Component
public class DummyJsonClient implements ExternalProductClient {

    private final RestClient client;

    public DummyJsonClient() {
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);

        factory.setReadTimeout(Duration.ofSeconds(5));

        this.client = RestClient.builder()
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }

    @Override
    public DummyJsonPage list(int limit, int skip) {
        return request(() -> client.get()
                .uri(b -> b.path("/products")
                        .queryParam("limit", limit)
                        .queryParam("skip", skip)
                        .build())
                .retrieve()
                .body(DummyJsonPage.class), false);
    }

    @Override
    public DummyJsonPage search(String query, int limit, int skip) {
        return request(() -> client.get()
                .uri(b -> b.path("/products/search")
                        .queryParam("q", query)
                        .queryParam("limit", limit)
                        .queryParam("skip", skip)
                        .build())
                .retrieve()
                .body(DummyJsonPage.class), false);
    }

    @Override
    public DummyJsonProduct findById(long id) {
        return request(() -> client.get()
                .uri("/products/{id}", id)
                .retrieve()
                .body(DummyJsonProduct.class), true);
    }

    private <T> T request(Supplier<T> supplier, boolean detail) {
        try {
            T result = supplier.get();

            if (result == null) {
                throw new ExternalApiException(
                        "외부 상품 응답이 비어 있습니다."
                );
            }

            return result;

        } catch (RestClientResponseException e) {
            if (detail && e.getStatusCode().value() == 404) {
                throw new NotFoundException(
                        "외부 상품을 찾을 수 없습니다."
                );
            }

            throw new ExternalApiException(
                    "외부 상품 서버 응답 오류", e
            );

        } catch (ResourceAccessException e) {
            throw new ExternalApiException(
                    "외부 상품 서버 연결 실패", e
            );

        } catch (RestClientException e) {
            throw new ExternalApiException(
                    "외부 상품 응답 처리 실패", e
            );
        }
    }
}
