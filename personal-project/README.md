# 취미 위시리스트 (Hobby Wishlist)

사고 싶은 취미 용품을 위시리스트에 모으고, **예산 안에서 살 수 있는 후보를 추천**받고, 마음에 드는 상품은 **장바구니**에 담아 합계를 확인하는 Spring Boot 개인 프로젝트입니다.
직접 입력한 상품뿐 아니라 외부 상품([DummyJSON](https://dummyjson.com))을 검색해 위시리스트로 가져올 수 있고, REST API와 간단한 웹 화면을 함께 제공합니다.

## 주요 기능

- **위시리스트**: 상품 등록·조회, 예산 이하 상품 추천(가격 낮은 순 최대 3개)
- **외부 상품 연동**: DummyJSON 상품을 검색하고 위시리스트로 가져오기 (재고 정보 포함)
- **장바구니**: 위시리스트 상품 담기, 수량 변경, 삭제, 합계 계산

## 기술 스택

- Java 21
- Spring Boot 4.1.2 (Spring Web MVC, Validation, `RestClient`)
- Gradle
- JUnit 5 / Spring Boot Test

## 실행 방법

```bash
# 서버 실행
./gradlew bootRun

# 테스트 실행
./gradlew test
```

서버가 실행되면 브라우저에서 <http://localhost:8080> 으로 접속합니다.

## 실행 화면

![메인 화면](docs/images/01-web-main.png)

![장바구니 화면](docs/images/18-web-cart.png)

- **위시리스트 추가**: 이름과 가격(0 이상의 정수)을 직접 입력하거나, 외부 상품을 검색해 가져옵니다.
- **예산별 추천**: 예산 이하의 아이템을 가격이 낮은 순으로 최대 3개 보여줍니다.
- **전체 목록**: 등록된 모든 아이템을 확인하고 장바구니에 담습니다.
- **장바구니**: 수량을 조절하고 항목을 삭제하며, 서버가 계산한 합계를 확인합니다.

## API 명세

| Method | Path | 설명 | 성공 응답 |
|---|---|---|---|
| POST | `/wishlist` | 아이템 등록 | 201 Created |
| GET | `/wishlist` | 전체 목록 조회 | 200 OK |
| GET | `/wishlist/{id}` | 단건 조회 | 200 OK |
| GET | `/wishlist?budget={n}` | 예산 이하 아이템 추천 (가격 낮은 순, 최대 3개) | 200 OK |
| POST | `/wishlist/import` | 외부 상품을 위시리스트에 추가 (이미 있으면 409) | 201 Created |
| GET | `/products?q={검색어}&limit={1~20}&skip={n}` | 외부 상품 목록/검색 (`q` 생략 시 전체) | 200 OK |
| GET | `/products/{id}` | 외부 상품 단건 조회 | 200 OK |
| POST | `/cart` | 위시리스트 아이템을 장바구니에 담기 (같은 상품이면 수량 합산) | 200 OK |
| GET | `/cart` | 장바구니 조회 | 200 OK |
| PATCH | `/cart/{cartItemId}` | 장바구니 수량 변경 | 200 OK |
| DELETE | `/cart/{cartItemId}` | 장바구니 항목 삭제 | 204 No Content |

요청/응답 예시와 스크린샷은 [위시리스트](docs/WISHLIST_EXAMPLES.md), [장바구니·외부 상품](docs/CART_EXAMPLES.md) 문서에 정리했습니다.

### 장바구니 `/cart`

요청 본문(JSON):

```json
{ "wishlistId": 1, "quantity": 2 }
```

수량 변경(`PATCH`)은 `{ "quantity": 3 }`을 보냅니다.

담기·수량 변경·조회는 모두 **장바구니 전체와 합계**를 응답합니다. 삭제(`DELETE`)만 본문 없이 204를 돌려줍니다.

```json
{
  "items": [
    { "id": 1, "wishlistId": 1, "name": "연필", "price": 500, "stock": null, "quantity": 2 }
  ],
  "subtotal": 1000
}
```

`stock`이 `null`이면 재고 제한이 없는 직접 등록 상품입니다.

![장바구니 담기](docs/images/14-api-cart-add-200.png)

장바구니 규칙:

- 같은 상품을 다시 담으면 새 항목을 만들지 않고 수량을 더합니다.
- 한 상품은 최대 5개까지(담은 수량 합산 기준) 담을 수 있습니다.
- 재고가 있는 상품은 재고를 초과해 담을 수 없습니다.
- 서로 다른 상품은 최대 20개까지 담을 수 있습니다.

## 예외 처리

모든 오류는 `GlobalExceptionHandler`에서 `ProblemDetail`(RFC 7807) 형식으로 응답합니다.

| 상황 | 상태 코드 | title | 스크린샷 |
|---|---|---|---|
| 존재하지 않는 id 조회 (위시리스트) | 404 | Item Not Found | [보기](docs/images/06-error-not-found-404.png) |
| 존재하지 않는 id 조회 (장바구니) | 404 | Item Not Found | [보기](docs/images/15-error-cart-item-not-found-404.png) |
| 중복 추가, 장바구니 규칙 위반 (수량/재고/개수 초과) | 409 | Conflict | [보기](docs/images/16-error-cart-stock-exceeded-409.png) |
| 경로/쿼리 파라미터 타입 불일치 | 400 | Type Mismatch | [보기](docs/images/07-error-type-mismatch-400.png) |
| 도메인 규칙 위반 (음수 가격 등) | 400 | Invalid Argument | [보기](docs/images/08-error-invalid-price-400.png) |
| 요청 본문 검증 실패 (필수 값 누락) | 400 | Validation Failed | [보기](docs/images/09-error-validation-failed-400.png) |

오류 응답 스크린샷은 [위시리스트](docs/WISHLIST_EXAMPLES.md#오류-응답), [장바구니](docs/CART_EXAMPLES.md#오류-응답) 문서에서 볼 수 있습니다.

## 프로젝트 구조

계층형 구조로 나누고, 의존성은 `presentation → application → domain ← infrastructure` 방향입니다.

```
src/main/java/com/example/hobby_wishlist
├── presentation     # Controller, GlobalExceptionHandler
├── application      # WishlistService, CartService, ExternalProductService
├── dto              # wishlist / cart(CartResponse 등) / externalproduct
├── domain           # 기능별 패키지: wishlist, cart, externalproduct
└── infrastructure   # persistence(인메모리 저장소), client(DummyJSON 연동)
```

- `WishlistItem`이 이름(공백 불가)과 가격(0 이상) 규칙을 스스로 검증합니다.
- `Cart`와 `CartItem`이 장바구니 규칙(수량, 재고, 최대 개수)을 스스로 검증하고, 소계·합계도 계산합니다.
- 저장소는 `WishlistRepository`, `CartRepository` 인터페이스 뒤에 인메모리 구현을 두어, 추후 DB로 교체할 수 있습니다.
- 외부 상품은 `ExternalProductProvider` 인터페이스 뒤에서 DummyJSON 클라이언트가 조회합니다. (외부 API 호출이 필요하므로 인터넷 연결이 필요합니다.)

## 테스트

도메인, 저장소, 서비스는 단위 테스트로, API는 통합 테스트로 검증합니다. 테스트 패키지는 `src/main`의 계층 구조를 따릅니다.

![gradlew test 실행 결과](docs/images/11-test-gradle-success.png)

![테스트 리포트](docs/images/12-test-report-summary.png)

| 테스트 클래스 | 대상 | 테스트 수 |
|---|---|---|
| `domain.WishlistItemTests` | 위시리스트 도메인 검증 규칙 | 3 |
| `domain.CartItemTests` | 장바구니 항목 수량 규칙, 소계 | 2 |
| `domain.CartTests` | 장바구니 담기·변경·삭제 규칙, 합계 | 10 |
| `infrastructure.InMemoryWishListRepositoryUnitTests` | 인메모리 저장소 | 2 |
| `application.WishlistServiceUnitTests` | 위시리스트 서비스 로직 | 10 |
| `application.CartServiceUnitTests` | 장바구니 서비스 로직 | 4 |
| `integration.HobbyWishlistApplicationTests` | API 통합 테스트 | 2 |

총 33개 테스트, 실패 0건.

## 한계 및 향후 계획

- 인메모리 저장소라 서버를 재시작하면 데이터가 초기화됩니다.
- 위시리스트의 수정·삭제 API는 아직 없습니다.
- 장바구니 저장소(`InMemoryCartRepository`)는 인메모리 구현만 있고, 서비스는 `synchronized`로 동시 접근을 처리합니다.
- 장바구니는 사용자 구분 없이 하나만 존재합니다.
- 쿠폰 기능은 구현하지 않았습니다.
