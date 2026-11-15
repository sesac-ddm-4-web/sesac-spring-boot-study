# 외부 상품 연동과 장바구니

옷 위시리스트(11/07)에 DummyJSON 외부 상품 가져오기와 장바구니·쿠폰 금액 미리보기를 더한 프로젝트입니다.
위시리스트와 장바구니는 MySQL에 저장합니다.

- 환경: Java 17, Spring Boot 4.1.2, Gradle Wrapper, MySQL 8.0.29(Docker)
- 실행: 아래 "DB 준비"를 마친 뒤 `personal-project/`에서 `./gradlew bootRun` → http://localhost:8080/
- 테스트: `./gradlew test` (DB 없이 실행됩니다)

## DB 준비

앱은 `dev` 프로파일로 실행되고 MySQL에 접속합니다. 처음 한 번만 아래 세 가지를 준비합니다.

1. MySQL 컨테이너 실행 (`<비밀번호>`는 직접 정합니다)

```bash
docker run --name personal-project-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=<비밀번호> -e MYSQL_DATABASE=personal_project -d mysql:8.0.29 --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
```

2. 테이블 생성 (`docker exec -it personal-project-mysql mysql -uroot -p personal_project`로 접속)

```sql
CREATE TABLE collection_items (
    id          BIGINT AUTO_INCREMENT PRIMARY KEY,
    name        VARCHAR(100) NOT NULL,
    price       INT          NOT NULL,
    source_type VARCHAR(20)  NOT NULL,
    external_id BIGINT       NULL,
    stock       INT          NULL,
    CONSTRAINT uk_collection_items_external_id UNIQUE (external_id)
);

CREATE TABLE cart_items (
    id       BIGINT PRIMARY KEY,
    item_id  BIGINT NOT NULL,
    quantity INT    NOT NULL,
    CONSTRAINT uk_cart_items_item_id UNIQUE (item_id),
    CONSTRAINT fk_cart_items_item FOREIGN KEY (item_id)
        REFERENCES collection_items (id) ON DELETE CASCADE
);
```

3. `src/main/resources/application-dev.yml` 작성 (비밀번호가 들어 있어 git에는 올리지 않습니다)

```yaml
spring:
  datasource:
    url: jdbc:mysql://localhost:3307/personal_project
    username: root
    password: <비밀번호>
    driver-class-name: com.mysql.cj.jdbc.Driver
```

| 프로파일 | 사용처 | 저장소 |
| --- | --- | --- |
| `dev` (기본) | `./gradlew bootRun` | `DatabaseCollectionItemRepository`, `DatabaseCartRepository` (MySQL) |
| `test` | `./gradlew test` | `ListCollectionItemRepository`, `MemoryCartRepository` (메모리, DB 연결 없음) |

## 단계별 완료 범위

| 단계 | 내용 | 상태 |
| --- | --- | --- |
| 1단계 | 외부 상품 목록·검색·상세 조회, 위시리스트로 가져오기 | 완료 |
| 2단계 | 장바구니 담기·조회·수량 변경·삭제, 쿠폰·배송비 금액 미리보기 | 완료 |
| 3단계 | AI 프론트엔드 (순수 HTML·CSS·JS, `static/index.html`) | 완료 |
| 3단계 | 최저 결제금액 쿠폰 추천 | 미구현 |

기존 위시리스트 등록·조회·수정·삭제·예산 추천과 테스트는 그대로 유지했습니다.

## API

| 요청 | 요청 값 | 성공 | 실패 |
| --- | --- | --- | --- |
| `GET /external-products` | `limit` 1~20(기본 10), `skip` 0 이상(기본 0) | 외부 상품 목록 | 400, 502 |
| `GET /external-products/search` | `q` 필수·공백 불가, `limit`, `skip` | 검색 결과 (없으면 `[]`) | 400, 502 |
| `GET /external-products/{id}` | | 외부 상품 1개 | 404, 502 |
| `POST /collectionItems/import` | `{"externalId":1}` | 저장된 위시리스트 항목 | 400, 404, 409, 502 |
| `POST /collectionItems` | `{"name":"코트","price":110000}` | 저장된 항목 | 400 |
| `GET /collectionItems`, `GET /collectionItems/{id}` | | 목록, 항목 1개 | 404 |
| `PUT /collectionItems/{id}` | `{"name":"롱코트","price":150000}` | 수정된 항목 | 400, 404, 외부 상품 409 |
| `DELETE /collectionItems/{id}` | | 200 | 404 |
| `GET /collectionItems/recommendations` | `budget` 0 이상 | 최대 3개 | 400 |
| `POST /cart/items` | `{"itemId":1,"quantity":2}` (위시리스트 id, 1~5) | 장바구니 | 400, 404, 409 |
| `GET /cart` | | `{items, subtotal}` | |
| `PATCH /cart/items/{cartItemId}` | `{"quantity":3}` | 장바구니 | 400, 404, 409 |
| `DELETE /cart/items/{cartItemId}` | | 200 | 404 |
| `GET /cart/preview` | `couponCode` `NONE`(기본)·`PERCENT10`·`FIXED15000` | `subtotal, discount, discountedSubtotal, shipping, finalAmount, coupon` | 400 |

위시리스트 항목 응답: `{"id":1,"name":"...","price":9990,"sourceType":"EXTERNAL","externalId":1,"stock":99}`
`id`는 내 위시리스트 id, `externalId`는 외부 상품 id입니다. 수동 상품은 `externalId`·`stock`이 `null`입니다.

실패 응답 본문은 사유 문자열입니다. 예: `quantity: 5 이하여야 합니다`, `이미 가져온 외부 상품입니다.`

## 규칙

- 가격 환산: 외부 price × 1,000을 `BigDecimal`로 계산해 `HALF_UP`으로 원 단위 반올림 (9.99 → 9,990원)
- 가져오기: 외부 id만 받고 이름·가격은 서버가 조회, 같은 외부 상품은 409, 품절도 저장 가능, 가져온 상품은 수정 불가(409), 저장 후 자동 갱신 없음
- 장바구니: 하나만 존재, 수량 1~5(합산 후에도 5), 최대 20종, 외부 상품은 저장된 재고 이하, 품절은 담기 불가, 수동 상품은 재고 제한 없음
- 참조: 장바구니에는 위시리스트 id와 수량만 저장하고 이름·가격·재고는 조회할 때마다 위시리스트에서 가져옴. 원본을 수정하면 장바구니에도 바로 반영되고, 원본을 삭제하면 장바구니 행도 함께 삭제
- 쿠폰: `PERCENT10` 10%(원 미만 버림, 최대 20,000원), `FIXED15000` 상품 합계 100,000원 이상일 때 15,000원, 한 번에 하나만
- 배송비: 할인 후 100,000원 이상 무료, 미만 5,000원. 빈 장바구니는 모두 0이고 `NONE`만 사용 가능
- 금액은 `long`으로 계산하고, 실패한 요청과 미리보기는 저장된 데이터를 바꾸지 않습니다.
- 외부 API 연결 3초·응답 5초 제한, 연결 실패·시간 초과는 502

## 클래스 책임

| 패키지 | 클래스 | 책임 |
| --- | --- | --- |
| `externalproduct.client` | `ExternalProductClient`, `DummyJsonClient` | 외부 API 호출 인터페이스와 RestClient 구현, HTTP 오류를 404·502 예외로 변환 |
| `externalproduct` | `ExternalProductService`, `ExternalProductController` | 외부 조회 조율, 입력 검증 |
| `item.domain` | `CollectionItem`, `SourceType`, `PriceConverter` | 상품 규칙(수동·외부 구분, 수정 가능 여부), 가격 환산 |
| `item.service` | `SimpleCollectionItemService`, `CollectionItemImportService` | 위시리스트 CRUD·추천, 가져오기 조율(검증 → 중복 확인 → 외부 조회 → 환산 → 저장) |
| `item.repository` | `CollectionItemRepository`, `DatabaseCollectionItemRepository`, `ListCollectionItemRepository` | 위시리스트 저장·조회 (`dev`는 MySQL, `test`는 메모리) |
| `cart.domain` | `Cart`, `CartItem`, `Coupon`, `PricePreview` | 종류·수량·재고 규칙, 저장된 행으로 장바구니 복원, 쿠폰·배송비·최종 금액 계산 |
| `cart.service` | `CartService` | 장바구니 작업 조율 (조회 → 변경 → 저장) |
| `cart.repository` | `CartRepository`, `DatabaseCartRepository`, `MemoryCartRepository` | 장바구니 조회·저장 (`dev`는 MySQL에서 위시리스트와 JOIN, `test`는 메모리) |
| `global.exception` | `GlobalExceptionHandler` 외 예외 | 400·404·409·502 응답 변환 |

Client와 Repository는 인터페이스 타입으로 생성자 주입합니다.

## 테스트

`./gradlew test`: 82개 통과

| 테스트 | 확인 내용 |
| --- | --- |
| `PriceConverterTest`, `CollectionItemTest` | 환산·반올림, 수동·외부 구분, 외부 상품 수정 불가 |
| `CollectionItemImportServiceTest` | 가짜 Client로 가져오기 성공·중복 409·없는 상품 404·외부 장애 502, 실패 후 목록 유지 |
| `CartTest` | 합산, 5개·재고·품절·20종 제한, 실패 후 수량 유지, 복사한 값 유지·재복사, long 합계 |
| `PricePreviewTest`, `CouponTest` | 110,000원 기준 쿠폰별 금액, 원 미만 버림, 2만 원 상한, 무료배송 경계, 빈 장바구니 |
| `CartServiceTest` | 원본 수정이 장바구니에 반영, 원본 삭제 시 장바구니에서 제외, 미리보기·실패 요청이 장바구니를 바꾸지 않음 |
| `ApiResponseTest` | 가짜 Client를 넣은 MockMvc로 상태 코드(400·404·409·502)와 사유 메시지 |
| `SimpleCollectionItemService*Test`, `DemoApplicationTests` | 기존 위시리스트 기능 |

## 남은 문제

- 최저 결제금액 쿠폰 추천(3단계 선택)은 구현하지 않았습니다.
- 중복 가져오기와 장바구니 동시 요청은 `synchronized`로 막아 서버 한 대에서만 안전합니다.
- 테이블은 직접 만들어야 하고, DB 저장소(`Database*Repository`)를 검증하는 자동 테스트는 없습니다.
- 장바구니 저장은 삭제와 추가·수정을 여러 SQL로 나눠 실행하며 트랜잭션으로 묶지 않았습니다.
- 외부 응답에 가격이 없으면 500이 납니다.
- 쿠폰 코드는 대문자만 받습니다.
