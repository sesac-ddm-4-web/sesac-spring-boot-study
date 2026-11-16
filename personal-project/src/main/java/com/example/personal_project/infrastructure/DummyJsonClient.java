package com.example.personal_project.infrastructure;

import java.net.http.HttpClient;
import java.time.Duration;

import org.springframework.http.HttpStatus;
import org.springframework.http.client.JdkClientHttpRequestFactory;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.client.RestClientResponseException;
import org.springframework.web.server.ResponseStatusException;

// DummyJSON에 상품 정보를 요청하는 클래스
@Component
public class DummyJsonClient {

    // 외부 HTTP 요청에 사용할 도구
    private final RestClient restClient;

    // 테스트에서 준비한 요청 도구를 전달받음
    DummyJsonClient(RestClient restClient) {
        this.restClient = restClient;
    }

    public DummyJsonClient() {

        // 연결은 최대 3초 대기
        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(3))
                .build();

        // 응답 읽기는 최대 5초 대기
        JdkClientHttpRequestFactory factory =
                new JdkClientHttpRequestFactory(httpClient);
        factory.setReadTimeout(Duration.ofSeconds(5));

        // 기본 주소와 시간 제한을 요청 도구에 연결
        this.restClient = RestClient.builder()
                .baseUrl("https://dummyjson.com")
                .requestFactory(factory)
                .build();
    }

    // 외부 상품 한 개 조회
    public DummyJsonProduct findById(long id) {

        // 잘못된 id는 외부 요청 전에 거부
        if (id <= 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "상품 id는 양수여야 합니다.");
        }

        try {

            // 외부 JSON 응답을 상품 DTO로 받음
            DummyJsonProduct product = restClient.get()
                    .uri("/products/{id}", id)
                    .retrieve() // 응답 처리 방법을 선택하는 단계
                    //응답 본문을 DummyJsonProduct 타입의 객체로 만들어서 돌려줘
                    .body(DummyJsonProduct.class);

            // 성공 응답이어도 본문이 없으면 502
            if (product == null) {

                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "외부 상품 응답이 비어 있습니다."
                );
            }

            return product;

        } catch (RestClientResponseException exception) {

            // 외부의 상품 없음 응답은 404로 전달
            if (exception.getStatusCode().value() == 404) {
                throw new ResponseStatusException(
                        HttpStatus.NOT_FOUND,
                        "외부 상품을 찾을 수 없습니다."
                );
            }

            // 나머지 외부 오류 응답은 502로 처리
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 서버가 오류를 반환했습니다."
            );
        } catch (RestClientException exception) {
            // 연결 실패 등 요청 중 발생한 오류는 502로 처리

            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 정보를 가져오지 못했습니다."
            );

        }
    }

    // 외부 상품 목록 조회
    public DummyJsonProductList findAll(int limit, int skip) {

        validatePaging(limit, skip);

        try {

            DummyJsonProductList response = restClient.get()
                    // limit: 조회 개수, skip: 건너뛸 개수
                    .uri("/products?limit={limit}&skip={skip}", limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);

            // 응답 자체가 없는 경우만 오류, 빈 상품 목록은 정상
            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "외부 상품 목록 응답이 비어 있습니다."
                );
            }
            return response;
        } catch (RestClientException exception) {
            // 연결 실패 등 요청 중 발생한 오류는 502로 처리
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 목록을 가져오지 못했습니다.");
        }
    }

    // 외부 상품 이름 검색
    public DummyJsonProductList search(String keyword, int limit, int skip) {

        // 검색어 누락 검사
        if (keyword == null) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "검색어를 입력해주세요.");
        }
        // 공백을 제거한 값을 다시 저장
        keyword = keyword.strip();

        // 빈 문자열과 공백뿐인 검색어 거부
        if (keyword.isEmpty()) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "검색어를 입력해주세요.");
        }

        validatePaging(limit, skip);

        try {

            DummyJsonProductList response = restClient.get()
                    // 검색은 /products/search, 검색어는 q로 전달
                    .uri("/products/search?q={keyword}&limit={limit}&skip={skip}",
                            keyword, limit, skip)
                    .retrieve()
                    .body(DummyJsonProductList.class);

            // 응답 자체가 없는 경우만 오류, 빈 상품 목록은 정상
            if (response == null) {
                throw new ResponseStatusException(
                        HttpStatus.BAD_GATEWAY,
                        "외부 상품 목록 응답이 비어 있습니다."
                );
            }
            return response;
        } catch (RestClientException exception) {
            // 연결 실패 등 요청 중 발생한 오류는 502로 처리
            throw new ResponseStatusException(
                    HttpStatus.BAD_GATEWAY,
                    "외부 상품 목록을 가져오지 못했습니다.");
        }
    }

    private void validatePaging(int limit, int skip) {
        // 조회 개수는 1~20개만 허용
        if (limit <1 || limit > 20) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "limit은 1 이상 20 이하여야 합니다."
            );
        }

        // 건너뛸 개수는 0 이상
        if (skip < 0) {
            throw new ResponseStatusException(
                    HttpStatus.BAD_REQUEST,
                    "skip은 0 이상이어야 합니다."
            );
        }
    }
}
