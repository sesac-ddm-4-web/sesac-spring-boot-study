# 위시리스트 & 예산 추천 서버

사고 싶은 물건을 위시리스트에 등록하고, 예산 안에서 살 수 있는 구매 후보를 최대 3개까지 추천하는 Spring Boot 서버입니다.

## 소개 · 환경

| 항목 | 내용 |
|---|---|
| 프로젝트 | 위시리스트 & 예산 추천 |
| Java | `21` |
| Spring Boot | `4.1.2` |
| 빌드 도구 | Gradle |
| 저장소 | 메모리(`CopyOnWriteArrayList`), 서버 재시작 시 초기화 |
| 프론트엔드 | 순수 HTML · CSS · JavaScript (`src/main/resources/static/index.html`) |

### 구조
```
presentation    ProductController, ProductImportController, CartController,
                ExternalProductController      HTTP 입출력, 예외 → 상태코드 변환
application     SimpleProductService, ExternalProductService, CartService
                                               업무 흐름 조율
domain          Product, SourceType, Cart, CartItem, Coupon, CartPreview,
                ProductRepository, CartRepository
                                               수량·재고·금액 규칙, 저장소 인터페이스
infrastructure  DatabaseProductRepository, DatabaseCartRepository
                                               MySQL 구현체
product         DummyJsonClient, DummyJsonProduct, DummyJsonProductList, PriceConverter
                                               외부 API 호출, 가격 환산
```
## 완료 기능 · API
### 위시리스트

| 기능 | 메서드 | 경로 | 요청 | 성공 | 실패 |
|---|---|---|---|---|---|
| 등록 | POST | `/products` | `{"name":"노트","price":12000}` | 200, id 부여 | 400 |
| 전체 목록 | GET | `/products` | - | 200, 등록 순서 | - |
| 단건 조회 | GET | `/products/{id}` | - | 200 | 404 |
| 예산 추천 | GET | `/recommend/{budget}` | - | 200, 최대 3개 | 400 |
| 외부 상품 가져오기 | POST | `/products/import/{externalId}` | - (id만 받음) | 200 | 400, 404, 409, 502 |

### 외부 상품 조회

| 기능 | 메서드 | 경로 | 실패 |
|---|---|---|---|
| 목록 | GET | `/external-products?limit=10&skip=0` | 400 (limit 1~20, skip 0 이상), 502 |
| 검색 | GET | `/external-products/search?q=` | 400 (q 누락·공백), 502 |
| 상세 | GET | `/external-products/{id}` | 400, 404, 502 |

### 장바구니 · 쿠폰

| 기능 | 메서드 | 경로 | 요청 | 성공 | 실패 |
|---|---|---|---|---|---|
| 담기 | POST | `/cart/items` | `{"productId":1,"quantity":2}` | 200 | 400, 404, 409 |
| 조회 | GET | `/cart` | - | 200 | - |
| 수량 변경 | PUT | `/cart/items/{rowId}` | `{"quantity":3}` | 200 | 400, 404, 409 |
| 삭제 | DELETE | `/cart/items/{rowId}` | - | 204 | 404 |
| 금액 미리보기 | GET | `/cart/preview?couponCode=` | - | 200 | 400 |
### 규칙
- 이름: null·공백 거부, 앞뒤 공백 제거 / 가격: 0 이상 정수(0원 허용) / 예산: 0 이상

**추천**: 예산 이하 → 가격 오름차순(같은 가격은 등록 순서) → 최대 3개 → 없으면 빈 목록. 원본 목록은 변경하지 않습니다.

**가격 환산**: `BigDecimal(외부 price) × 1,000` 후 `HALF_UP`으로 원 단위 정수 반올림 (예: 9.99 → 9,990원). 저장한 값은 자동 갱신하지 않습니다.

**장바구니**
- 한 번에 담는 수량 1~5, 같은 상품은 새 줄 없이 수량 합산(합산 후에도 최대 5), 최대 20종
- 외부 상품은 저장된 stock 이하, 품절은 담기 불가 / 수동 상품은 재고 제한 없음
- 처음 담을 때 이름·단가·재고를 복사해 이후에도 그 값을 사용, 삭제 후 다시 담으면 현재 값을 새로 복사


### 에러 응답

| 상태 | 조건 | 본문 |
|---|---|---|
| 400 | 이름이 비어 있음 | `이름은 비어 있을 수 없습니다.` |
| 400 | 가격이 음수 | `가격은 0 이상의 정수여야 합니다.` |
| 400 | 예산이 음수 | `예산은 0 이상이어야 합니다.` |
| 404 | 존재하지 않는 id | `<실제 응답 본문>` |

## 테스트

| 대상 | 종류 | 검증 내용 |
|---|---|---|
| `domain/CartTest` | 단위 | 수량 1~5, 같은 상품 합산, 20종 제한, 재고·품절, 복사값 유지, 실패 시 상태 유지 |
| `application/SimpleProductServiceTest` | 단위(mock) | 이름·가격·예산 검증, 등록, 추천 규칙(경계·정렬·동률·최대 3개) |
| `application/CartServiceTest` | 통합(MySQL) | 담기·합산, 5개 초과·재고 초과 시 DB 불변, 수량 변경·삭제, 삭제 후 재담기 복사, 쿠폰·배송비 미리보기 |
| `infrastructure/DatabaseProductRepositoryTest` | 통합(MySQL) | 저장·복원, 외부 필드(`source_type`, `external_id`, `stock`), `ORDER BY id`, UNIQUE 제약 |
| `infrastructure/DatabaseCartRepositoryTest` | 통합(MySQL) | 저장·조회·수량 변경·삭제, 복사본 유지, `product_id` UNIQUE |
| `presentation/ProductControllerTest` | 통합 | API 상태코드(200/400/404) |
| `presentation/SimpleProductServiceDbTest` | 통합(MySQL) | DB 기준 추천 규칙, 동률 시 등록 순서 유지 |


## 트랜잭션

| 요청 |적용 |
|---|---|
| 장바구니 담기·수량 변경·삭제·조회·미리보기 | 1회 (조회 → 규칙 검사 → 저장) | `@Transactional` |

물품의 수량과 관계가 있는 경우 `@Transactional`을 사용함