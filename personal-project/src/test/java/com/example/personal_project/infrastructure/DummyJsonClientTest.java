package com.example.personal_project.infrastructure;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.http.HttpMethod;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.test.web.client.MockRestServiceServer;
import org.springframework.web.client.RestClient;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.net.ConnectException;
import java.net.http.HttpTimeoutException;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.method;
import static org.springframework.test.web.client.match.MockRestRequestMatchers.requestTo;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withException;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withStatus;
import static org.springframework.test.web.client.response.MockRestResponseCreators.withSuccess;

// Client 자체를 검사
public class DummyJsonClientTest {
    private DummyJsonClient client;
    // 테스트에서 정한 응답을 돌려주는 도구
    private MockRestServiceServer server;

    // 실제 외부 서버 대신 테스트에서 반환할 JSON
    private static final String PRODUCT_JSON = """
            {
              "id": 1,
              "title": "마스카라",
              "price": 9.99,
              "category": "beauty",
              "thumbnail": "https://example.com/product.webp",
              "stock": 99,
              "rating": 4.5
            }
            """;

    private static final String PRODUCT_LIST_JSON =
            "{\"products\":[" + PRODUCT_JSON + "],\"total\":100,\"skip\":5,\"limit\":20}";

    // 각 테스트를 실행하기 전에 이 메서드를 먼저 실행 (테스트마다 새 겍체 준비/설정 초기화)
    @BeforeEach
    void setUp() {
        // builder: RestClient를 만들기 위한 설정을 담은 객체 -> 기본 주소 지정
        // 기본 주소가 DummyJSON인 요청 도구를 만들 준비
        RestClient.Builder builder = RestClient.builder()
                .baseUrl("https://dummyjson.com");

        // 실제 네트워크 요청 대신 준비한 응답을 돌려주도록 연결
        // 이 설정으로 만들 요청 도구는, 실제 사이트 대신 가짜 응답을 받도록
        server = MockRestServiceServer.bindTo(builder).build();
        client = new DummyJsonClient(builder.build());
//        client → DummyJsonClient 객체
//             └── 안에 RestClient 객체를 보관
    }

    @Test
    @DisplayName("외부 상품 JSON을 DTO로 받고 사용하지 않는 필드는 무시한다")
    void 외부_상품_상세_조회_성공() {
        // 요청을 기다릴 조건 / 응답 준비
        // 요청이 왔을 때 주소·방식을 확인(올바른 요청인지)
        server.expect(requestTo("https://dummyjson.com/products/1")) // Client가 이 주소로 요청해야 해, 안그럼 실패
                .andExpect(method(HttpMethod.GET)) // 요청 방식은 GET
                .andRespond(withSuccess(PRODUCT_JSON, MediaType.APPLICATION_JSON)); // 그 요청이 오면 200 성공 응답을 돌려줘
        
        // 실제 Client가 상품 조회 코드 실행 -> 준비된 JSON -> DTO -> product에 저장
        DummyJsonProduct product = client.findById(1L);

        // 기대값과 실제값 비교
        assertEquals(Long.valueOf(1L), product.id());
        assertEquals("마스카라", product.title());
        assertEquals(new BigDecimal("9.99"), product.price());
        assertEquals("beauty", product.category());
        assertEquals("https://example.com/product.webp", product.thumbnail());
        assertEquals(Integer.valueOf(99), product.stock());

        // 기대한다고 설정한 요청이 실제로 들어왔는지 확인
        // 요청이 아예 안왔을 때
        server.verify();
    }

    @Test
    @DisplayName("0과 음수 id는 외부 요청 전에 400으로 거부한다")
    void 잘못된_상품_id는_외부_요청_전_거부() {
        ResponseStatusException zeroException = assertThrows(
                ResponseStatusException.class, () -> client.findById(0L));

        ResponseStatusException negativeException = assertThrows(
                ResponseStatusException.class, () -> client.findById(-1L));

        // 0이거나 음수일 때 예외 상태가 400인지 검사
        assertEquals(HttpStatus.BAD_REQUEST, zeroException.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, negativeException.getStatusCode());
    }

    @Test
    @DisplayName("외부 상세 조회의 404 응답은 404 예외로 전달한다")
    void 외부_상품이_없으면_404() {
        server.expect(requestTo("https://dummyjson.com/products/999999"))
                .andRespond(withStatus(HttpStatus.NOT_FOUND));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findById(999999L));

        assertEquals(HttpStatus.NOT_FOUND, exception.getStatusCode());
        assertEquals("외부 상품을 찾을 수 없습니다.", exception.getReason());
//        server → HTTP 404를 제공
//        Client → 그 응답을 처리하고 예외를 던짐
//        assertThrows → 예외를 잡고 확인한 뒤 exception 변수로 돌려줌

        // 아까 기다리기로 한 상품 999999번 조회 요청, 실제로 들어왔어?
        server.verify();
    }

    @Test
    @DisplayName("외부 상세 조회의 서버 오류는 502로 처리한다")
    void 외부_상세_서버_오류는_502() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR)); // 500

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findById(1L));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode()); // 502
        assertEquals("외부 상품 서버가 오류를 반환했습니다.", exception.getReason());

        server.verify();
    }

    @Test
    @DisplayName("상세 조회가 성공해도 응답 본문이 없으면 502로 처리한다")
    void 외부_상세_응답_본문이_없으면_502() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findById(1L));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("외부 상품 응답이 비어 있습니다.", exception.getReason());

        server.verify();
    }

    @Test
    @DisplayName("외부 상세 조회의 연결 실패는 502로 처리한다")
    void 외부_상세_연결_실패시_502() {
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withException(new ConnectException("테스트용 연결 실패")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findById(1L));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("외부 상품 정보를 가져오지 못했습니다.", exception.getReason());
        server.verify();
    }

    @Test
    @DisplayName("외부 상세 조회에서 시간 초과 예외가 발생하면 502로 처리한다")
    void 외부_상세_시간_초과_예외는_502() {
        // 실제로 시간을 기다리지 않고 시간 초과 예외를 재현
        server.expect(requestTo("https://dummyjson.com/products/1"))
                .andRespond(withException(new HttpTimeoutException("테스트용 시간 초과")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findById(1L));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("외부 상품 정보를 가져오지 못했습니다.", exception.getReason());
        server.verify();
    }

    @Test
    @DisplayName("목록 조회 시 limit과 skip을 전달하고 상품 목록을 받는다")
    void 외부_상품_목록_조회_성공() {
        server.expect(requestTo("https://dummyjson.com/products?limit=20&skip=5"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PRODUCT_LIST_JSON, MediaType.APPLICATION_JSON));

        DummyJsonProductList response = client.findAll(20, 5);

        assertEquals(1, response.products().size());
        assertEquals(Long.valueOf(1L), response.products().get(0).id());
        assertEquals("마스카라", response.products().get(0).title());
        server.verify();
    }

    @Test
    @DisplayName("목록 조회 결과가 없으면 빈 상품 목록을 반환한다")
    void 외부_목록_결과가_없으면_빈_목록() {
        server.expect(requestTo("https://dummyjson.com/products?limit=10&skip=9999"))
                .andRespond(withSuccess("{\"products\":[]}", MediaType.APPLICATION_JSON));

        DummyJsonProductList response = client.findAll(10, 9999);

        assertEquals(List.of(), response.products());
        server.verify();
    }

    @Test
    @DisplayName("목록의 잘못된 limit과 skip은 요청 전에 400으로 거부한다")
    void 외부_목록_페이징_입력_오류는_400() {
        ResponseStatusException tooSmall = assertThrows(
                ResponseStatusException.class, () -> client.findAll(0, 0));
        ResponseStatusException tooLarge = assertThrows(
                ResponseStatusException.class, () -> client.findAll(21, 0));
        ResponseStatusException negativeSkip = assertThrows(
                ResponseStatusException.class, () -> client.findAll(10, -1));

        assertEquals(HttpStatus.BAD_REQUEST, tooSmall.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, tooLarge.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, negativeSkip.getStatusCode());
        server.verify();
    }

    @Test
    @DisplayName("목록 조회 응답 본문이 없으면 502로 처리한다")
    void 외부_목록_응답_본문이_없으면_502() {
        server.expect(requestTo("https://dummyjson.com/products?limit=10&skip=0"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findAll(10, 0));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("외부 상품 목록 응답이 비어 있습니다.", exception.getReason());
        server.verify();
    }

    @Test
    @DisplayName("외부 목록 서버의 오류 응답은 502로 처리한다")
    void 외부_목록_서버_오류는_502() {
        server.expect(requestTo("https://dummyjson.com/products?limit=10&skip=0"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.findAll(10, 0));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    @DisplayName("검색어 앞뒤 공백을 제거하고 q와 페이징 값을 전달한다")
    void 외부_검색어_공백_제거_후_검색() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone&limit=1&skip=0"))
                .andExpect(method(HttpMethod.GET))
                .andRespond(withSuccess(PRODUCT_LIST_JSON, MediaType.APPLICATION_JSON));

        DummyJsonProductList response = client.search("  phone  ", 1, 0);

        assertEquals(1, response.products().size());
        assertEquals(Long.valueOf(1L), response.products().get(0).id());
        server.verify();
    }

    @Test
    @DisplayName("검색어의 공백과 특수 문자를 요청 주소에 안전하게 담는다")
    void 외부_검색어_특수_문자_전달() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone%20%26%20case&limit=10&skip=0"))
                .andRespond(withSuccess("{\"products\":[]}", MediaType.APPLICATION_JSON));

        DummyJsonProductList response = client.search("phone & case", 10, 0);

        assertEquals(List.of(), response.products());
        server.verify();
    }

    @Test
    @DisplayName("검색 결과가 없으면 오류 없이 빈 목록을 반환한다")
    void 외부_검색_결과가_없으면_빈_목록() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=zzzznonexistent987654&limit=10&skip=0"))
                .andRespond(withSuccess("{\"products\":[]}", MediaType.APPLICATION_JSON));

        DummyJsonProductList response = client.search("zzzznonexistent987654", 10, 0);

        assertEquals(List.of(), response.products());
        server.verify();
    }

    @Test
    @DisplayName("null과 빈 문자열 및 공백 검색어는 요청 전에 400으로 거부한다")
    void 외부_검색어_누락과_빈값은_400() {
        ResponseStatusException missing = assertThrows(
                ResponseStatusException.class, () -> client.search(null, 10, 0));
        ResponseStatusException empty = assertThrows(
                ResponseStatusException.class, () -> client.search("", 10, 0));
        ResponseStatusException blank = assertThrows(
                ResponseStatusException.class, () -> client.search("   ", 10, 0));

        assertEquals(HttpStatus.BAD_REQUEST, missing.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, empty.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, blank.getStatusCode());
        server.verify();
    }

    @Test
    @DisplayName("검색의 잘못된 limit과 skip은 요청 전에 400으로 거부한다")
    void 외부_검색_페이징_입력_오류는_400() {
        ResponseStatusException tooSmall = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 0, 0));
        ResponseStatusException tooLarge = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 21, 0));
        ResponseStatusException negativeSkip = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 10, -1));

        assertEquals(HttpStatus.BAD_REQUEST, tooSmall.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, tooLarge.getStatusCode());
        assertEquals(HttpStatus.BAD_REQUEST, negativeSkip.getStatusCode());
        server.verify();
    }

    @Test
    @DisplayName("검색 응답 본문이 없으면 502로 처리한다")
    void 외부_검색_응답_본문이_없으면_502() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone&limit=10&skip=0"))
                .andRespond(withSuccess("", MediaType.APPLICATION_JSON));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 10, 0));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        assertEquals("외부 상품 목록 응답이 비어 있습니다.", exception.getReason());
        server.verify();
    }

    @Test
    @DisplayName("외부 검색 서버의 오류 응답은 502로 처리한다")
    void 외부_검색_서버_오류는_502() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone&limit=10&skip=0"))
                .andRespond(withStatus(HttpStatus.INTERNAL_SERVER_ERROR));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 10, 0));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }

    @Test
    @DisplayName("외부 검색 중 연결이 실패하면 502로 처리한다")
    void 외부_검색_연결_실패시_502() {
        server.expect(requestTo("https://dummyjson.com/products/search?q=phone&limit=10&skip=0"))
                .andRespond(withException(new ConnectException("테스트용 연결 실패")));

        ResponseStatusException exception = assertThrows(
                ResponseStatusException.class, () -> client.search("phone", 10, 0));

        assertEquals(HttpStatus.BAD_GATEWAY, exception.getStatusCode());
        server.verify();
    }
}
