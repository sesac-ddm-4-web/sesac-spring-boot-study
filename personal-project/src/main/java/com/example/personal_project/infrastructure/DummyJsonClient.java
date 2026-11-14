package com.example.personal_project.infrastructure;

import com.example.personal_project.domain.exception.CommonBadGateWayException;
import com.example.personal_project.domain.exception.CommonBadRequestException;
import com.example.personal_project.domain.exception.CommonNotFoundException;
import com.example.personal_project.presentation.dto.dummyJsonProduct.DummyJsonProduct;
import com.example.personal_project.presentation.dto.dummyJsonProduct.DummyJsonProductResponse;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

import java.net.http.HttpClient;
import java.time.Duration;

@Component
public class DummyJsonClient {
    private final RestClient restClient;

    public DummyJsonClient(){
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

    public DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public DummyJsonProduct findById(Long id){
        if(id<=0){
            throw new CommonBadRequestException("상품id는 양수입니다");
        }
        try {
            DummyJsonProduct product = restClient.get()
                    .uri("/products/{id}",id)
                    .retrieve()
                    .body(DummyJsonProduct.class);
            if (product == null){
                throw new CommonBadGateWayException("외부 상품이 비어있습니다");
            }
            return product;
        }catch (RestClientResponseException exception){
            if (exception.getStatusCode().value() == 404){
                throw new CommonNotFoundException("외부 상품을 찾을 수 없습니다.");
            }
            throw new CommonBadGateWayException("외부 상품 서버가 오류를 반환했습니다");
        }catch (RestClientException exception){
            throw new CommonBadGateWayException("외부 상품 정보를 가져오지 못했습니다");
        }
    }

    public DummyJsonProductResponse getProducts(int limit, int skip){
        try {
            DummyJsonProductResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products")
                            .queryParam("limit", limit)
                            .queryParam("skip", skip)
                            .build())
                    .retrieve()
                    .body(DummyJsonProductResponse.class);
            if (response == null || response.products() == null) {
                throw new CommonBadGateWayException("외부 상품 목록 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientResponseException e) {
            throw new CommonBadGateWayException("외부 상품 서버가 오류를 반환했습니다.");
        } catch (RestClientException e) {
            throw new CommonBadGateWayException("외부 상품 정보를 가져오지 못했습니다.");
        }
    }

    public DummyJsonProductResponse searchProducts(String keyword, int limit, int skip){
        try {
            DummyJsonProductResponse response = restClient.get()
                    .uri(uriBuilder -> uriBuilder
                            .path("/products/search")
                            .queryParam("q",keyword)
                            .queryParam("limit",limit)
                            .queryParam("skip",skip)
                            .build())
                    .retrieve()
                    .body(DummyJsonProductResponse.class);
            if (response == null || response.products() == null) {
                throw new CommonBadGateWayException("외부 상품 검색 응답이 비어 있습니다.");
            }
            return response;
        } catch (RestClientResponseException e) {
            throw new CommonBadGateWayException("외부 상품 서버가 오류를 반환했습니다.");
        } catch (RestClientException e) {
            throw new CommonBadGateWayException("외부 상품 검색에 실패했습니다.");
        }
    }
}
