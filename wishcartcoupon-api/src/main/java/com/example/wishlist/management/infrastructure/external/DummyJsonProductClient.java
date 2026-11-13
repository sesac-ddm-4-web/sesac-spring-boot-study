package com.example.wishlist.management.infrastructure.external;

import com.example.wishlist.management.domain.external.ExternalProduct;
import com.example.wishlist.management.domain.external.ExternalProductClient;
import com.example.wishlist.management.domain.exception.ExternalApiException;
import com.example.wishlist.management.domain.exception.NotFoundException;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;

import java.math.BigDecimal;
import java.util.List;

// infrastructure/DummyJsonProductClient.java
@Component
@ConditionalOnProperty(name="external.catalog", havingValue="dummyjson", matchIfMissing=true)
public class DummyJsonProductClient implements ExternalProductClient {

    private final RestClient restClient;

    public DummyJsonProductClient(RestClient dummyJsonRestClient) {   // 생성자 주입
        this.restClient = dummyJsonRestClient;
    }

    @Override
    public List<ExternalProduct> list(int limit, int skip) {
        return fetchList("/products?limit={limit}&skip={skip}", limit, skip);
    }

    @Override
    public List<ExternalProduct> search(String q, int limit, int skip) {
        return fetchList("/products/search?q={q}&limit={limit}&skip={skip}", q, limit, skip);
    }

    @Override
    public ExternalProduct get(long id) {
        try {
            Item item = restClient.get().uri("/products/{id}", id)
                    .retrieve().body(Item.class);
            if (item == null) {
                throw new ExternalApiException("외부 API 응답이 비어 있습니다.", null);
            }
            return item.toExternalProduct();
        } catch (HttpClientErrorException.NotFound e) {
            throw new NotFoundException("외부 상품을 찾을 수 없습니다. id=" + id);
        } catch (RestClientException e) {   // 연결 실패, 타임아웃, 그 외 오류
            throw new ExternalApiException("외부 API 호출에 실패했습니다.", e);
        }
    }

    private List<ExternalProduct> fetchList(String uri, Object... vars) {
        try {
            ItemsResponse response = restClient.get().uri(uri, vars)
                    .retrieve().body(ItemsResponse.class);
            if (response == null || response.products() == null) {
                return List.of();
            }
            return response.products().stream().map(Item::toExternalProduct).toList();
        } catch (RestClientException e) {
            throw new ExternalApiException("외부 API 호출에 실패했습니다.", e);
        }
    }

    // JSON을 읽기 위한 내부 전용 모양. 우리가 안 쓰는 필드는 무시한다.
    @JsonIgnoreProperties(ignoreUnknown = true)
    record Item(long id, String title, BigDecimal price,
                String category, String thumbnail, int stock) {
        ExternalProduct toExternalProduct() {
            return new ExternalProduct(id, title, price, category, thumbnail, stock);
        }
    }

    @JsonIgnoreProperties(ignoreUnknown = true)
    record ItemsResponse(List<Item> products) {}
}
