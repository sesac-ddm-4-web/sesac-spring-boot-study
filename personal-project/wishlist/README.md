# 취미 위시리스트, 외부 상품 연동, 장바구니, DB 연동

사고 싶은 물건을 등록하고 예산 안에서 추천받는 위시리스트에, DummyJSON 외부 상품 검색과 가져오기, 장바구니(쿠폰, 배송비 미리보기)를 더한 API입니다.
위시리스트와 장바구니는 MySQL에 저장하므로 서버를 다시 시작해도 데이터가 남습니다.

## 환경

| 항목 | 버전 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 (spring-boot-starter-webmvc, spring-boot-starter-validation, spring-boot-starter-jdbc) |
| DB | MySQL 8.0.29 (Docker 컨테이너), `mysql-connector-j`, `NamedParameterJdbcTemplate` |
| 외부 호출 | Spring Web에 포함된 `RestClient` (별도 HTTP 라이브러리 없음) |
| 빌드 | Gradle 9.8.2 (Wrapper 포함) |
| 테스트 | JUnit 5, MockMvc, MySQL 테스트 DB(`wishlist_test`) |
| 화면 | 순수 HTML, CSS, JavaScript (외부 라이브러리, CDN 없음) |

## 실행과 테스트

실행 전 준비는 [1-3. 실행 설정](#1-3-실행-설정)을 먼저 확인합니다. MySQL 컨테이너가 켜져 있어야 서버와 테스트가 동작합니다.

`personal-project/wishlist` 폴더에서 실행합니다.

```bash
./gradlew bootRun   # 서버 실행 → http://localhost:8080
./gradlew test      # 테스트 실행
```

Windows 명령 프롬프트에서는 `gradlew.bat bootRun`, `gradlew.bat test`를 사용합니다.
서버를 실행한 뒤 브라우저에서 `http://localhost:8080/wish.html`을 열면 위시리스트 화면이 나옵니다.

---

## 1. DB 연동

메모리(`ArrayList`) 저장소를 MySQL 저장소로 바꿨습니다. Controller, Service, API 계약은 그대로이고, Service는 계속 Repository **인터페이스**만 압니다.

```
Controller → Service → CollectionItemRepository / CartItemRepository (인터페이스)
                          ├─ 실행: DatabaseCollectionItemRepository, DatabaseCartItemRepository (MySQL)
                          └─ 단위 테스트: ListCollectionItemRepository, ListCartItemRepository (src/test, 메모리)
```

### 1-1. 테이블

전체 DDL: [`src/main/resources/schema.sql`](src/main/resources/schema.sql)

**`collection_items`** (위시리스트)

| 컬럼 | 자료형 | NULL | 제약 | 자바 필드 | 설명 |
|---|---|---|---|---|---|
| `id` | BIGINT | X | PK, AUTO_INCREMENT | `Long id` | 내 로컬 id. DB가 부여 |
| `name` | VARCHAR(100) | X | | `String name` | 수동 상품은 같은 이름 허용 (UNIQUE 없음) |
| `price` | INT | X | | `int price` | 원 단위. 0 허용 |
| `source_type` | VARCHAR(20) | X | UNIQUE ① | `SourceType sourceType` | enum을 문자열로 저장 (`MANUAL`, `DUMMYJSON`) |
| `external_id` | BIGINT | O | UNIQUE ① | `Long externalId` | 외부 상품 id. 수동 상품은 NULL |
| `stock` | INT | O | | `Integer stock` | 가져올 때의 외부 재고. 수동 상품은 NULL |

① `uk_collection_items_source_external UNIQUE (source_type, external_id)`: 같은 출처의 같은 외부 상품은 한 줄만. MySQL UNIQUE는 NULL끼리 중복으로 보지 않으므로 `external_id`가 NULL인 수동 상품은 여러 개 저장됩니다.

**`cart_items`** (장바구니 한 줄)

| 컬럼 | 자료형 | NULL | 제약 | 자바 필드 | 설명 |
|---|---|---|---|---|---|
| `id` | BIGINT | X | PK, AUTO_INCREMENT | `Long id` | 장바구니 행 id. 수량 변경, 삭제에 사용 |
| `wish_item_id` | BIGINT | X | UNIQUE ② | `Long wishItemId` | 원본 위시리스트 로컬 id (FK 없음) |
| `name` | VARCHAR(100) | X | | `String name` | 처음 담을 때 복사한 이름 |
| `unit_price` | BIGINT | X | | `long unitPrice` | 처음 담을 때 복사한 단가 |
| `stock` | INT | O | | `Integer stock` | 처음 담을 때 복사한 재고. 수동 상품은 NULL (재고 제한 없음) |
| `quantity` | INT | X | | `int quantity` | 1~5 (범위 검사는 domain) |

② `uk_cart_items_wish_item UNIQUE (wish_item_id)`: 같은 원본은 장바구니에 한 줄 (다시 담으면 합산).

**FK를 걸지 않은 이유**

장바구니는 원본을 참조하는 대신 담는 순간의 이름, 단가, 재고를 복사해 둡니다. 원본 위시를 수정, 삭제해도 장바구니 행과 원본 id는 그대로 남아야 합니다 (기존 규칙).

| 선택지 | 위시 삭제 시 | 맞지 않는 이유 |
|---|---|---|
| `ON DELETE CASCADE` | 장바구니 행도 삭제 | 장바구니를 함께 지우면 안 됨 |
| `ON DELETE RESTRICT` | 위시 삭제를 막음 | 기존 API는 장바구니에 담긴 상품도 삭제 가능 (동작 변경) |
| `ON DELETE SET NULL` | `wish_item_id`가 NULL | 원본 로컬 id를 보존하지 못함 |
| **FK 없음** | 장바구니 행 그대로 | 원본 존재 확인은 담을 때 Service가 함 (없으면 404) |

### 1-2. SQL

모두 `NamedParameterJdbcTemplate`의 이름 있는 파라미터(`:name`)를 씁니다. 조회 결과는 `BeanPropertyRowMapper`로 객체에 채웁니다.

| Repository 메서드 | SQL | 비고 |
|---|---|---|
| 위시 `add` | `INSERT INTO collection_items (name, price, source_type, external_id, stock) VALUES (...)` | `GeneratedKeyHolder`로 DB가 만든 id를 받아 객체에 넣음. enum은 `VARCHAR`로 등록해 문자열로 저장 |
| 위시 `findById` | `SELECT ... FROM collection_items WHERE id=:id` | 없으면 `null` (Service가 404) |
| 위시 `findByExternalId` | `SELECT ... WHERE source_type=:sourceType AND external_id=:externalId` | 중복 가져오기 확인 |
| 위시 `findAll` | `SELECT ... FROM collection_items ORDER BY id` | 등록 순서. 추천의 같은 가격 순서도 이 순서를 따름 |
| 위시 `update` | `UPDATE collection_items SET name = :name, price = :price WHERE id = :id` | 바뀌는 값(이름, 가격)만 |
| 위시 `delete` | `DELETE FROM collection_items WHERE id=:id` | 지운 행 수 > 0이면 `true` (0이면 Service가 404) |
| 장바구니 `add` | `INSERT INTO cart_items (wish_item_id, name, unit_price, stock, quantity) VALUES (...)` | |
| 장바구니 `findById`, `findByWishItemId` | `SELECT ... FROM cart_items WHERE id=:id` / `WHERE wish_item_id=:wishItemId` | 없으면 `null` |
| 장바구니 `findAll` | `SELECT ... FROM cart_items ORDER BY id` | 담은 순서 |
| 장바구니 `update` | `UPDATE cart_items SET quantity = :quantity WHERE id = :id` | 바뀌는 값(수량)만 |
| 장바구니 `delete` | `DELETE FROM cart_items WHERE id=:id` | |

**변경 저장 순서**

DB에서 읽은 객체는 복사본이라 값을 바꿔도 DB에 반영되지 않습니다. 그래서 Repository에 `update`를 추가하고, Service는 "domain 규칙 검사와 값 변경 → `update`" 순서로 호출합니다. 규칙 검사에서 예외가 나면 `update`까지 가지 않으므로 실패한 요청은 DB를 바꾸지 않습니다.

| Service 메서드 | 순서 |
|---|---|
| `WishService.update` | `findById` → 외부 상품 거부, 입력 검증 → `item.update(...)` → `repository.update(item)` |
| `CartService.add` (이미 담긴 상품) | `findByWishItemId` → `addQuantity` (5개, 재고 검사) → `cartRepository.update(...)` |
| `CartService.changeQuantity` | `findById` → `changeQuantity` (범위, 재고 검사) → `cartRepository.update(...)` |

**409 변환**

중복은 Service가 먼저 확인해 `DuplicateItemException`(409)을 냅니다. 확인과 저장 사이에 같은 요청이 동시에 들어와 DB UNIQUE에 걸리면 Spring이 `DuplicateKeyException`을 던지고, `GlobalExceptionHandler`가 이것도 409로 바꿉니다.

### 1-3. 실행 설정

1. MySQL 컨테이너 실행 (호스트 3306은 다른 MySQL이 쓰고 있어 3307을 사용)

   ```bash
   docker run -d --name some-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD=<비밀번호> mysql:8.0.29
   ```

   이후에는 `docker start some-mysql`로 켭니다.

2. 스키마 생성 (테이블은 서버, 테스트가 시작할 때 `schema.sql`로 만듭니다)

   ```sql
   CREATE DATABASE wishlist;
   CREATE DATABASE wishlist_test;
   ```

3. 비밀번호 파일 만들기: `src/main/resources/application-local.yaml` (`.gitignore`에 포함, 커밋하지 않음)

   ```yaml
   spring:
     datasource:
       password: <비밀번호>
   ```

   파일 대신 환경 변수 `DB_PASSWORD`로 넣어도 됩니다.

| 파일 | 내용 |
|---|---|
| `src/main/resources/application.yaml` | `jdbc:mysql://localhost:3307/wishlist`, `root`, `application-local.yaml` 가져오기, `spring.sql.init.mode: always` |
| `src/main/resources/application-local.yaml` | DB 비밀번호 (커밋하지 않음) |
| `src/main/resources/schema.sql` | `CREATE TABLE IF NOT EXISTS` 두 개. 시작할 때마다 실행되지만 기존 테이블, 데이터는 건드리지 않음 |
| `src/test/resources/application-test.yaml` | `test` 프로필일 때 URL만 `wishlist_test`로 교체 |

DB를 쓰는 테스트(`ApiStatusTest`, `Database...RepositoryTest`)는 `@ActiveProfiles("test")`로 `wishlist_test`만 사용하고, 테스트마다 두 테이블을 `TRUNCATE`해서 개발용 `wishlist` 데이터는 지우지 않습니다.

### 1-4. Repository 통합 테스트

`wishlist_test`에 실제 SQL을 실행합니다. 단위 테스트는 메모리 저장소를 쓰므로 SQL, 컬럼, 객체 복원 문제는 이 테스트로 확인합니다.

| 테스트 | 확인 항목 | 분류 |
|---|---|---|
| `DatabaseCollectionItemRepositoryTest.manualItemRoundTrip` | DB id 부여, `source_type`에 문자열 `MANUAL` 저장, NULL인 `externalId`, `stock`이 0이 아닌 `null`로 복원 | SQL, 객체 복원 |
| `externalItemRoundTrip` | `DUMMYJSON`, `externalId`, `stock` 복원, source + externalId 조회 | 객체 복원 |
| `findAllKeepsInsertOrder` | `ORDER BY id` 등록 순서, 같은 가격 추천도 등록 순서 | SQL, 기존 규칙 |
| `update` | 이름, 가격만 바뀌고 출처는 그대로 | SQL |
| `failedUpdateKeepsRow` | 외부 상품 수정 거부, 음수 가격 거부 뒤 DB 값 유지 | 실패 보존 |
| `manualNameDuplicateAllowed` | 같은 이름 수동 상품 두 개 저장 | 제약 |
| `externalUniqueConstraint` | 같은 외부 상품 중복 저장은 `DuplicateKeyException`, 기존 행 유지 | 제약, 실패 보존 |
| `notFoundAndDelete` | 없는 id는 `null`, 삭제는 한 번만 `true` | SQL |
| `DatabaseCartItemRepositoryTest.roundTrip` | 행 id, 원본 id, 수량, 이름, 단가, 재고 복사본 복원 | 객체 복원 |
| `manualStockIsNull` | 수동 상품 행의 재고 NULL 복원 | 객체 복원 |
| `findAllKeepsInsertOrder` | `ORDER BY id` 담은 순서 | SQL |
| `quantityUpdate` | 합산, 수량 변경이 DB에 반영, 한 줄 유지 | SQL |
| `failedQuantityKeepsRow` | 합산 초과, 재고 초과(409), 범위 밖(400) 뒤 DB 수량 유지 | 실패 보존 |
| `wishItemUniqueConstraint` | 같은 원본 두 줄 저장은 `DuplicateKeyException`, 기존 행 유지 | 제약, 실패 보존 |
| `originalChangesDoNotTouchCart` | 원본 수정, 삭제 뒤에도 장바구니 복사본, 원본 id 유지, 수량 변경 가능 | FK 없음 설계 |
| `notFoundAndDelete` | 없는 행 `null`, 삭제는 한 번만 `true` | SQL |

### 1-5. 수동 QA

서버를 실행하고 `http://localhost:8080/wish.html` 또는 Postman으로 확인합니다. 시작 전 `wishlist`의 두 테이블을 비우고, 응답과 함께 `SELECT * FROM collection_items;`, `SELECT * FROM cart_items;`로 DB도 확인합니다.

| 분류 | 요청 | 기대 결과 | 실제 결과 |
|---|---|---|---|
| 정상 | 수동 상품 등록 `키보드`, 50000 | 200, DB에 `MANUAL`, `external_id`·`stock` NULL | |
| 정상 | 같은 이름 `키보드`, 0원 한 번 더 등록 | 200, 새 id로 두 번째 행 (이름 중복, 0원 허용) | |
| 정상 | 외부 상품 1번 가져오기 | 200, DB에 `DUMMYJSON`, `external_id` 1, `stock` 저장 | |
| 정상 | 첫 번째 `키보드`를 장바구니에 2개 → 1개 더 | 200, `cart_items` 한 줄, `quantity` 3 | |
| 정상 | 첫 번째 `키보드` 이름, 가격 수정 | 200, `collection_items` 반영, `cart_items`의 이름, 단가는 그대로 | |
| 실패 | 외부 상품 1번 다시 가져오기 | 409, 행 수 그대로 | |
| 실패 | 장바구니의 `키보드` 3개 더 담기 (합계 6) | 409, `quantity` 3 그대로 | |
| 실패 | 가격 -1로 등록 | 400, 행 수 그대로 | |
| 실패 | 없는 id 조회 `GET /wish/items/999` | 404 | |
| 재시작 | 서버 종료 → 다시 실행 → 위시 목록, 장바구니 조회 | 재시작 전 데이터, id, 수량 그대로 | |
| 재시작 | 재시작 후 새 상품 등록 | id가 1부터가 아니라 이어서 증가 | |
| 재시작 | 재시작 후 외부 상품 1번 다시 가져오기 | 409 (중복 규칙이 재시작 뒤에도 유지) | |

### 1-6. 완료 범위

- [x] 위시리스트, 장바구니를 MySQL 테이블 두 개에 저장 (`schema.sql`)
- [x] id: DB `AUTO_INCREMENT`. 로컬 id와 외부 `source_type` + `external_id` 구분 유지
- [x] 외부 상품 조합 DB UNIQUE, 충돌 409 변환. 수동 상품 이름 중복 허용
- [x] 장바구니 행 id, 원본 로컬 id, 수량, 이름·단가·재고 복사본 보존. FK 없이 원본 삭제와 분리
- [x] 목록 SQL `ORDER BY id`, 추천 동률 등록 순서 유지
- [x] 가격 0, 입력 검증, 400, 404, 409, 502 등 기존 API 동작 유지
- [x] 외부 요청 실패 시 저장하지 않고 기존 DB 유지 (검사 통과 후에만 `add`)
- [x] 별도 테스트 DB에서 Repository 통합 테스트 16개 (SQL, 객체 복원, 실패 보존, 제약)
- [x] 기존 단위 테스트(검증, 추천, 쿠폰 계산) 수정 없이 유지
- [x] DB 비밀번호는 커밋하지 않는 `application-local.yaml`로 분리

---

## 2. API 계약

### 요청 형식

- 기존 위시리스트 API(`/wish/items` 등록, 수정, `/wish/recommendations`)는 그대로 **쿼리 파라미터 또는 form 값**을 사용합니다 (`wish.html`이 이 형식을 씁니다).
- 이번에 추가한 쓰기 API(가져오기, 장바구니 담기, 수량 변경)는 **JSON 본문**을 사용합니다. `Content-Type: application/json`
- 조회 조건(`q`, `limit`, `skip`, `couponCode`)은 쿼리 파라미터입니다.
- 실패 응답 본문은 에러 메시지 문자열입니다.

### 위시리스트

| 기능 | 요청 | 성공 | 실패 |
|---|---|---|---|
| 등록 | `POST /wish/items` (`name`, `price`) | 200, 등록된 아이템 | 400 |
| 전체 목록 | `GET /wish/items` | 200, 등록 순서 목록 | |
| 단건 조회 | `GET /wish/items/{id}` | 200, 아이템 | 404 |
| 수정 (수동 상품만) | `PUT /wish/items/{id}` (`name`, `price`) | 200 | 400 (외부 상품 포함), 404 |
| 삭제 | `DELETE /wish/items/{id}` | 204 | 404 |
| 추천 | `GET /wish/recommendations?budget=...` | 200, 최대 3개 | 400 |
| 외부 상품 가져오기 | `POST /wish/items/import` `{"externalId": 1}` | 200, 저장된 아이템 | 400, 404, 409, 502 |

아이템 응답 예: `{"id":1,"name":"Essence Mascara Lash Princess","price":9990,"sourceType":"DUMMYJSON","externalId":1,"stock":99,"external":true}`

### 외부 상품 (DummyJSON)

브라우저 → 내 Spring 서버 → `https://dummyjson.com`

| 기능 | 요청 | 호출하는 외부 API | 실패 |
|---|---|---|---|
| 목록 | `GET /external-products?limit=10&skip=0` | `GET /products?limit=&skip=` | 400, 502 |
| 검색 | `GET /external-products/search?q=phone&limit=10&skip=0` | `GET /products/search?q=` | 400, 502 |
| 상세 | `GET /external-products/{id}` | `GET /products/{id}` | 400, 404, 502 |
| 수업 예제 | `GET /examples/external-products/{id}` | `GET /products/{id}` (가격 환산 없이 원본) | |

응답 예: `[{"id":1,"title":"Essence Mascara Lash Princess","price":9990,"category":"beauty","thumbnail":"https://...","stock":99}]`

### 장바구니

| 기능 | 요청 | 성공 | 실패 |
|---|---|---|---|
| 조회 | `GET /cart` | 200 `{"items":[...],"totalQuantity":3,"subtotal":110000}` | |
| 담기 | `POST /cart/items` `{"wishItemId": 1, "quantity": 2}` | 200, 장바구니 행 | 400, 404, 409 |
| 수량 변경 | `PATCH /cart/items/{cartItemId}` `{"quantity": 3}` | 200, 장바구니 행 | 400, 404, 409 |
| 삭제 | `DELETE /cart/items/{cartItemId}` | 204 | 404 |
| 금액 미리보기 | `GET /cart/preview?couponCode=PERCENT10` | 200 | 400 |

장바구니 행 예: `{"id":1,"wishItemId":2,"name":"monitor","unitPrice":55000,"stock":null,"quantity":2,"lineTotal":110000}`

미리보기 예: `{"subtotal":110000,"discount":11000,"discountedSubtotal":99000,"shipping":5000,"finalAmount":104000,"coupon":"PERCENT10"}`

### 상태 코드

| 상태 | 언제 |
|---|---|
| 400 | 잘못된 입력 (DTO 검증 실패, JSON 형식 오류, 숫자 자리에 문자), 알 수 없거나 사용할 수 없는 쿠폰, 외부 상품 수정 시도 |
| 404 | 없는 로컬 상품, 외부 상품, 장바구니 행 |
| 409 | 외부 상품 중복 가져오기, 상품 종류, 수량, 재고 규칙 충돌 |
| 502 | 외부 API 연결 실패, 시간 초과(연결 3초, 읽기 5초), 오류 응답, 이름이나 가격이 빈 응답 |

모든 검사를 먼저 하고 통과한 뒤에만 저장하거나 값을 바꾸므로, 실패한 가져오기, 추가, 변경, 미리보기는 기존 데이터를 바꾸지 않습니다.

### 규칙

#### 위시리스트 (기존)

| 대상 | 규칙 |
|---|---|
| 이름 | `null`, 빈 문자열, 공백만 있는 이름은 거부. 앞뒤 공백은 제거해서 저장 |
| 가격 | 0 이상의 정수. 무료 아이템(0원) 허용 |
| id | 저장할 때 DB가 `AUTO_INCREMENT`로 부여. 등록 응답에 담긴 실제 id로 조회 |
| 실패한 등록 | 검증을 모두 통과한 뒤에만 저장하므로 목록과 다음 id가 바뀌지 않음 |
| 예산, 추천 | 예산 0 이상. 개별 가격이 예산 이하인 물건만, 가격 오름차순, 같은 가격은 등록 순서 유지, 최대 3개 |

#### 외부 상품

| 대상 | 규칙 |
|---|---|
| 검색어 | `q`가 없거나 공백이면 400. 결과가 없으면 200과 `[]` |
| limit, skip | `limit` 기본 10, 1~20. `skip` 기본 0, 0 이상 |
| 읽는 필드 | `id, title, price, category, thumbnail, stock`만. `discountPercentage`, `minimumOrderQuantity` 등은 무시 |
| 가져오기 | 외부 상품 id만 받고, 이름과 가격은 서버가 상세 API에서 조회 |
| id 구분 | 내 위시리스트 `id`와 `sourceType`(`MANUAL`/`DUMMYJSON`), `externalId`를 따로 저장 |
| 중복 | 같은 외부 상품을 다시 가져오면 409 (외부 API를 부르기 전에 확인) |
| 품절 | `stock` 0인 상품도 위시리스트에는 저장 |
| 수정, 갱신 | 가져온 상품의 이름, 가격 수정은 400. 외부 값이 바뀌어도 자동 갱신하지 않음 |
| 가격 환산 | 외부 `price` 1단위 = 1,000원. `BigDecimal`로 1,000을 곱하고 `HALF_UP`으로 원 단위 반올림 (`9.99` → `9990`). 수업용 고정 규칙이며 실제 환율이 아님 |

#### 장바구니

장바구니 하나를 DB(`cart_items`)에 저장합니다 (사용자 구분 없음). 재고 예약, 차감이나 결제는 하지 않습니다.

| 대상 | 규칙 |
|---|---|
| 수량 | 요청 수량은 1~5 (벗어나면 400). 같은 상품을 다시 담으면 새 줄 없이 합산, 합산 후 5 초과는 409 |
| 종류 | 최대 20종. 21번째 새 상품은 409 |
| 재고 | 외부 상품은 저장된 `stock` 이하 (초과 409), 품절은 담을 수 없음 (409). 수동 상품은 제한 없음 |
| 원본 | 담을 때 위시리스트에 원본 상품이 있어야 함 (없으면 404) |
| 복사 | 처음 담을 때 이름, 단가, 재고를 복사. 원본이 수정, 삭제돼도 유지. 중복 추가, 수량 변경도 복사한 값 사용 |
| 다시 담기 | 장바구니에서 삭제한 뒤 다시 담으면 현재 위시리스트 값을 새로 복사 |
| 행 id | 수량 변경, 삭제는 장바구니 행 id를 쓰므로 원본이 삭제돼도 가능 |
| 금액 | 단가 × 수량, 합계, 할인, 배송비는 `long`으로 계산 |

#### 쿠폰, 배송비 미리보기

`couponCode`를 생략하면 `NONE`. 쿠폰은 한 번에 하나만 적용하며, 미리보기는 저장하거나 장바구니를 바꾸지 않습니다.

| 쿠폰 | 할인 (상품 합계 `subtotal` 기준) |
|---|---|
| `NONE` | 없음 |
| `PERCENT10` | 10%, 원 미만 버림, 최대 20,000원 |
| `FIXED15000` | 상품 합계 100,000원 이상일 때 15,000원 (미만이면 400) |

- 할인 후 금액(`discountedSubtotal`)이 100,000원 이상이면 무료배송, 미만이면 배송비 5,000원
- `finalAmount = discountedSubtotal + shipping`
- 빈 장바구니는 모든 금액이 0이고 `NONE`만 사용 가능 (다른 쿠폰은 400)
- 모르는 쿠폰 코드는 400

| 상품 합계 110,000원 | 할인 | 배송비 | 최종 금액 |
|---|---|---|---|
| NONE | 0원 | 0원 | 110,000원 |
| PERCENT10 | 11,000원 | 5,000원 | 104,000원 |
| FIXED15000 | 15,000원 | 5,000원 | 100,000원 |

---

## 3. 클래스 책임

```
src/main/java/com/example/wishlist
├─ presentation/   HTTP 입력과 응답, DTO 검증, 예외 → 상태 코드
│  └─ dto/         요청 DTO, 응답 DTO
├─ application/    작업 순서 조율
├─ domain/         업무 규칙과 Repository 인터페이스
└─ infrastructure/ DB 저장(JDBC), 외부 API 호출
```

| 계층 | 맡는 일 | 하지 않는 일 |
|---|---|---|
| Controller | HTTP 요청을 DTO로 받기, `@Valid`, `@Min` 같은 입력 형식 검사, 응답 DTO로 변환 | 할인 계산, 저장, 외부 호출 |
| Service | "원본 찾기 → 규칙 검사 맡기기 → 저장" 순서 조율 | 규칙 숫자(5개, 20종, 10%)를 직접 다루기 |
| Domain | 수량, 재고, 종류, 금액 규칙 | HTTP, 저장 방법, 외부 API |
| Repository | SQL로 저장, 조회, 수정, 삭제 | 규칙 검사 |
| Client | DummyJSON 호출, 연결 3초, 읽기 5초 제한, 실패 → 404/502 | 저장, 가격 환산 |

| 패키지 | 클래스 | 책임 |
|---|---|---|
| presentation | `WishController` | 위시리스트 등록, 조회, 수정, 삭제, 추천, 외부 상품 가져오기 |
| | `ExternalProductController` | 외부 상품 목록, 검색, 상세. `limit`, `skip`, `q`, `id` 검사 |
| | `CartController` | 장바구니 조회, 담기, 수량 변경, 삭제, 금액 미리보기 |
| | `ExternalProductExampleController` | 수업 예제. DummyJSON 원본 응답 그대로 |
| | `GlobalExceptionHandler` | 모든 Controller의 예외를 400, 404, 409, 502로 변환 |
| presentation/dto | `ImportRequest`, `CartAddRequest`, `CartQuantityRequest` | 요청 본문 모양과 입력 검사 (`@NotNull`, `@Min`, `@Max`) |
| | `ExternalProductResponse`, `CartResponse`, `CartItemResponse`, `PaymentPreviewResponse` | domain 객체를 응답 모양으로 변환 |
| application | `WishService` | 위시리스트 등록, 수정(외부 상품 거부), 삭제, 추천 |
| | `ExternalProductService` | 외부 조회, 가져오기 순서 (중복 확인 → 외부 상세 조회 → 저장) |
| | `CartService` | 담기, 변경, 삭제, 미리보기 순서. 규칙 검사는 domain에 맡김 |
| domain | `CollectionItem`, `SourceType` | 위시리스트 상품과 출처 (`MANUAL`, `DUMMYJSON`) |
| | `ExternalProduct`, `PriceConverter` | 원화로 정리한 외부 상품, ×1,000 HALF_UP 환산 |
| | `CartItem` | 장바구니 한 줄. 이름, 단가, 재고 복사본과 수량 1~5, 재고, 품절 규칙 |
| | `Cart` | 장바구니 전체. 최대 20종, 총 수량, 상품 합계 |
| | `Coupon` | 쿠폰 코드 해석과 할인 계산 |
| | `PaymentPreview` | 할인 후 금액, 배송비, 최종 금액 |
| | `CollectionItemRepository`, `CartItemRepository` | 저장소 인터페이스 (Service는 이것만 앎) |
| | `ItemNotFoundException`, `CartItemNotFoundException` | 404 |
| | `DuplicateItemException`, `CartConflictException` | 409 |
| infrastructure | `DatabaseCollectionItemRepository`, `DatabaseCartItemRepository` | Repository 인터페이스의 MySQL 구현 (`NamedParameterJdbcTemplate`) |
| (src/test) | `ListCollectionItemRepository`, `ListCartItemRepository` | 단위 테스트용 `ArrayList` 구현 |
| | `DummyJsonClient` | DummyJSON 호출, 시간 제한, 실패 → 404/502 |
| | `DummyJsonProduct`, `DummyJsonProductList` | DummyJSON 응답 모양 그대로의 DTO |

- Service는 생성자로 Repository **인터페이스**와 `DummyJsonClient`를 받습니다. 테스트에서는 Spring 없이 `new CartService(new ListCartItemRepository(), ...)`처럼 만들고, 외부 호출은 `FakeDummyJsonClient`로 바꿔 끼웁니다.

---

## 4. 단계별 완료 범위

### 기존
- [x] 위시리스트 등록, 목록, 단건 조회, 예산 추천과 테스트 (변경 없이 유지)

### 1단계 (필수): 외부 상품 검색, 조회 → 내 위시리스트로 가져오기
- [x] DummyJSON 목록, 검색, 상세 조회 (`limit`, `skip` 기본값과 범위 검사)
- [x] 외부 가격 원화 환산 (×1,000, BigDecimal, HALF_UP)
- [x] 외부 상품 id만 받아 서버가 이름, 가격을 조회해 저장, 중복 409, 품절도 저장
- [x] 가져온 상품 수정 거부, 수동 상품 수정, 삭제
- [x] 외부 호출을 `DummyJsonClient`로 분리, 시간 제한, 실패 시 404/502

### 2단계 (필수): 장바구니 수량 관리 → 쿠폰, 배송비 포함 금액 미리보기
- [x] 장바구니 담기, 조회, 수량 변경, 삭제 (1~5개, 합산, 20종, 재고, 품절)
- [x] 처음 담을 때 이름, 단가, 재고 복사
- [x] 쿠폰 `NONE`, `PERCENT10`, `FIXED15000`과 배송비, 최종 금액 미리보기 (저장하지 않음)
- [x] 400, 404, 409, 502 상태 코드와 실패 시 데이터 보존
- [x] 계층 분리: Controller(입력, DTO 검증) / Service(조율) / Domain(규칙) / Repository(저장) / Client(외부 호출)

### 3단계 (선택): 최저 결제금액 쿠폰 추천, AI 프론트엔드
- [x] 최저 결제금액 쿠폰 추천: 화면이 쿠폰 3개를 모두 미리보기로 요청하고, 최종 결제금액이 가장 낮은 쿠폰에 '최저' 표시 (금액 계산은 서버, 화면은 비교만)
- [x] `wish.html` 한 페이지에 위시리스트(수정, 삭제, 장바구니 담기), 외부 상품 검색과 가져오기, 장바구니와 쿠폰 미리보기를 모두 표시. 순수 HTML, CSS, JavaScript만 사용

---

## 5. QA, 테스트

`./gradlew test` 결과: **55개 모두 통과** (기존 39개 + Repository 통합 테스트 16개). 외부 호출은 `FakeDummyJsonClient`로 바꿔서 인터넷 없이 실행됩니다. DB를 쓰는 테스트는 MySQL `wishlist_test`가 필요합니다.

### 자율 체크리스트

| 분류 | 확인 항목 | 확인한 테스트 | 결과 |
|---|---|---|---|
| 금액 | 9.99 → 9,990원 (HALF_UP) | `PriceConverterTest` | 통과 |
| 금액 | 110,000원 표: NONE 110,000, PERCENT10 104,000, FIXED15000 100,000 | `PaymentPreviewTest.table`, `ApiStatusTest.preview` | 통과 |
| 금액 | PERCENT10 원 미만 버림 (9,999 → 999), 최대 20,000원 | `PaymentPreviewTest.percentRule` | 통과 |
| 금액 | FIXED15000은 100,000원 이상만, 99,999원은 400 | `PaymentPreviewTest.fixedRule` | 통과 |
| 금액 | 배송비 경계: 할인 후 100,000원 무료, 99,999원 5,000원 | `PaymentPreviewTest.shippingBoundary` | 통과 |
| 금액 | 빈 장바구니는 모두 0원, NONE 외 쿠폰은 400 | `PaymentPreviewTest.emptyCart`, `ApiStatusTest.emptyPreview` | 통과 |
| 실패 보존 | 합산 후 6개(409) 뒤에도 수량 5 그대로 | `CartServiceTest.mergeSameItem` | 통과 |
| 실패 보존 | 수량 0, 6(400), 재고 초과(409), 없는 행(404) 뒤에도 수량 그대로 | `CartServiceTest.invalidChangeKeepsState` | 통과 |
| 실패 보존 | 21번째 상품(409) 뒤에도 20종 그대로 | `CartServiceTest.maxKinds` | 통과 |
| 실패 보존 | 중복 가져오기(409), 없는 외부 상품(404), 외부 장애(502) 때 위시리스트에 저장 안 됨 | `ExternalProductServiceTest.rejectDuplicate`, `importFailureKeepsData`, `ApiStatusTest.badGateway` | 통과 |
| 실패 보존 | 미리보기(성공, 실패 모두) 뒤에도 장바구니 그대로 | `CartServiceTest.previewDoesNotChangeCart` | 통과 |
| 복사본 | 원본 이름, 가격을 바꿔도 장바구니는 처음 값 유지, 중복 추가도 처음 단가 | `CartServiceTest.keepCopiedValues` | 통과 |
| 복사본 | 원본을 삭제해도 장바구니 행 유지, 수량 변경, 삭제 가능 | `CartServiceTest.originalDeleted` | 통과 |
| 복사본 | 장바구니에서 지우고 다시 담으면 현재 값을 새로 복사 | `CartServiceTest.readdCopiesCurrentValue` | 통과 |
| 상태 코드 | 실제 HTTP 요청으로 400, 404, 409, 502 | `ApiStatusTest` | 통과 |

| 테스트 클래스 | 개수 | 범위 |
|---|---|---|
| `WishServiceTest` | 5 | 기존 위시리스트 |
| `PriceConverterTest`, `PaymentPreviewTest` | 2, 6 | 금액 규칙 (Spring 없이) |
| `ExternalProductServiceTest`, `CartServiceTest` | 6, 12 | 작업 순서와 실패 보존 (Spring 없이) |
| `ApiStatusTest` | 8 | HTTP 상태 코드 (`@SpringBootTest`, MockMvc, `wishlist_test`) |
| `DatabaseCollectionItemRepositoryTest`, `DatabaseCartItemRepositoryTest` | 8, 8 | SQL, 객체 복원, 실패 보존, 제약 (`wishlist_test`, 1-4 참고) |

### 실제 결과 (서버를 실행해 실제 DummyJSON으로 요청)

```
GET  /external-products/search?q=laptop&limit=1          → 200 [{"id":78,"title":"Apple MacBook Pro 14 Inch Space Grey","price":1999990,...,"stock":24}]
POST /wish/items/import {"externalId":78}                → 200 {"id":1,"externalId":78,"sourceType":"DUMMYJSON","price":1999990,"stock":24,...}
POST /wish/items/import {"externalId":78}                → 409 이미 위시리스트에 가져온 외부 상품입니다. (외부 상품 id: 78)
POST /wish/items?name=monitor&price=55000                → 200 {"id":2,"sourceType":"MANUAL",...}
POST /cart/items {"wishItemId":2,"quantity":2}           → 200 {"id":1,"unitPrice":55000,"quantity":2,"lineTotal":110000,...}
GET  /cart/preview?couponCode=PERCENT10                  → 200 {"subtotal":110000,"discount":11000,"discountedSubtotal":99000,"shipping":5000,"finalAmount":104000,"coupon":"PERCENT10"}
GET  /cart/preview?couponCode=FIXED15000                 → 200 {"subtotal":110000,"discount":15000,"discountedSubtotal":95000,"shipping":5000,"finalAmount":100000,"coupon":"FIXED15000"}
POST /cart/items {"wishItemId":1,"quantity":1}           → 200 (맥북 1,999,990원 추가)
GET  /cart/preview?couponCode=PERCENT10                  → 200 {"subtotal":2109990,"discount":20000,...,"shipping":0,"finalAmount":2089990}   ← 최대 20,000원
GET  /cart/preview?couponCode=BOGUS                      → 400 알 수 없는 쿠폰입니다. (BOGUS)
POST /cart/items {"wishItemId":2,"quantity":4}           → 409 한 상품은 최대 5개까지 담을 수 있습니다. (현재 2개)
PATCH /cart/items/1 {"quantity":0}                       → 400 수량은 1개부터 5개까지 가능합니다.
GET  /cart                                               → 200 실패한 요청 뒤에도 monitor 수량 2 그대로
```

```
POST /wish/items  name="   "  price=1000             → 400 이름을 입력해 주세요.
POST /wish/items  name=다이어리  price=-1            → 400 가격은 0원 이상이어야 합니다.
GET /wish/items/99     → 404 해당 id의 아이템을 찾을 수 없습니다. (id: 99)
GET /wish/recommendations?budget=1500   → 200 [스티커 0, 펜 1500, 마스킹 테이프 1500]
GET /wish/recommendations?budget=1499   → 200 [스티커 0]
```

---

## 6. 남은 문제

| 문제 | 현재 상태 |
|---|---|
| 장바구니가 하나뿐 | 사용자 구분이 없어서 모든 요청이 같은 장바구니를 씀 |
| 동시 요청 | 중복 저장은 DB UNIQUE가 막지만, 같은 장바구니 행의 수량을 동시에 바꾸는 경우(읽고 → 바꾸고 → 저장 사이 경합)는 고려하지 않음 |
| 외부 값 갱신 | 가져온 뒤 DummyJSON의 가격, 재고가 바뀌어도 반영하지 않음 (조건대로). 장바구니 재고도 처음 복사한 값 기준 |
| 실패 응답 형식 | 메시지 문자열만 보냄. 에러 코드가 담긴 JSON 형식은 아님 |
| 요청 형식이 섞임 | 기존 위시리스트 API는 form 값, 새 쓰기 API는 JSON (기존 화면을 바꾸지 않으려고 유지) |
| 한글 검색어 | Git Bash의 curl로 한글을 그대로 보내면 서버에 닿기 전에 400. `%ED%82%A4`처럼 인코딩하면 정상 (브라우저는 문제없음) |
| 화면 테스트 | 화면은 브라우저에서 직접 확인했고, 자동 테스트는 없음 |
