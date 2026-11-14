package com.example.wishlistbudget.external.client;

import com.example.wishlistbudget.exception.ExternalApiException;
import com.example.wishlistbudget.exception.ExternalProductNotFoundException;
import com.example.wishlistbudget.external.dto.DummyJsonProduct;
import com.example.wishlistbudget.external.dto.DummyJsonProductList;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;

@Component
public class DummyJsonClient {

    private final RestClient restClient;

    public DummyJsonClient(
            @Qualifier("DummyJsonRestClient") RestClient restClient
    ) {
        this.restClient = restClient;
    }

    // 외부 상품 단건 조회
    public DummyJsonProduct getProduct(Long id) {
        try {
            DummyJsonProduct product = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve()
                    .body(DummyJsonProduct.class);

            if (product == null
                    || product.id() == null
                    || product.title() == null
                    || product.price() == null
                    || product.stock() == null) {
                throw new ExternalApiException(
                        "외부 상품 응답 데이터가 올바르지 않습니다."
                );
            }

            return product;

        } catch (HttpClientErrorException.NotFound e) {
            throw new ExternalProductNotFoundException(id);

        } catch (RestClientException e) {
            throw new ExternalApiException(
                    "외부 상품 조회 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    // 외부 상품 목록 조회
    public DummyJsonProductList getProducts(int limit, int skip) {
        try {
            DummyJsonProductList result = restClient.get()
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);

            if (result == null || result.products() == null) {
                throw new ExternalApiException(
                        "외부 상품 목록 응답 데이터가 올바르지 않습니다."
                );
            }

            return result;

        } catch (RestClientException e) {
            throw new ExternalApiException(
                    "외부 상품 목록 조회 중 오류가 발생했습니다.",
                    e
            );
        }
    }

    // 외부 상품 검색
    public DummyJsonProductList searchProducts(String q) {
        try {
            DummyJsonProductList result = restClient.get()
                    .uri("/products/search?q={q}", q)
                    .retrieve()
                    .body(DummyJsonProductList.class);

            if (result == null || result.products() == null) {
                throw new ExternalApiException(
                        "외부 상품 검색 응답 데이터가 올바르지 않습니다."
                );
            }

            return result;

        } catch (RestClientException e) {
            throw new ExternalApiException(
                    "외부 상품 검색 중 오류가 발생했습니다.",
                    e
            );
        }
    }
}