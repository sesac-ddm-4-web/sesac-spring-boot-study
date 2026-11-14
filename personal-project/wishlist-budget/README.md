# Wishlist Budget

Spring Boot 기반 위시리스트 및 장바구니 관리 REST API 프로젝트입니다.

사용자가 원하는 상품을 위시리스트에 등록하고, 예산에 맞는 상품을 추천받을 수 있습니다. 외부 상품 API를 통해 상품을 검색하고 가져올 수 있으며, 장바구니 수량 관리와 쿠폰 할인, 배송비 계산 기능을 제공합니다.

초기에는 `ArrayList` 기반 메모리 저장소를 사용했고, 이후 **Spring JDBC와 파일 기반 H2**로 전환하여 데이터 영속성을 구현했습니다. 현재는 **실행용 데이터베이스를 MySQL 8.4.8로 변경**했으며, 테스트에서는 별도의 H2 데이터베이스를 사용합니다.

---

## 1. 기술 스택

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle |
| Web | Spring MVC |
| Database | MySQL 8.4.8 (애플리케이션), H2 (테스트) |
| Database Access | Spring JDBC, JdbcTemplate |
| External API | DummyJSON |
| HTTP Client | Spring RestClient |
| Validation | Jakarta Bean Validation |
| Test | JUnit 5, Mockito |
| API Test | Postman |
| Frontend | HTML, CSS, JavaScript |

JPA를 사용하지 않고 JDBC로 SQL을 직접 실행하여 데이터베이스에 접근하도록 구현했습니다.

---

## 2. 주요 기능

### 2.1. 위시리스트 관리

- 상품 이름과 가격을 입력하여 위시리스트 등록
- 등록된 전체 상품 조회
- 상품 ID를 이용한 단건 조회
- 예산 이하의 상품 중 가격이 낮은 순서대로 최대 3개 추천
- 상품 이름 공백 및 음수 가격에 대한 유효성 검사
- 직접 등록한 상품과 외부 상품 구분
- JDBC를 통한 데이터베이스 저장 및 조회

가격이 동일한 상품은 기존 등록 순서를 유지하도록 처리했습니다.

### 2.2. 외부 상품 API 연동

[DummyJSON](https://dummyjson.com/) API를 이용하여 외부 상품 정보를 조회합니다.

- 외부 상품 목록 조회
- 검색어를 이용한 상품 검색
- 외부 상품 ID를 이용한 상세 조회
- 외부 상품을 위시리스트로 가져오기
- 동일 외부 상품 중복 등록 방지
- 외부 API 오류 및 타임아웃 처리

외부 상품 등록 시 클라이언트는 상품 ID만 전달합니다.

서버가 직접 외부 API를 호출하여 상품 이름, 가격, 재고를 조회한 뒤 데이터베이스에 저장합니다.

**외부 상품 가격 변환**

```java
BigDecimal convertedPrice = product.price()
        .multiply(BigDecimal.valueOf(1000))
        .setScale(0, RoundingMode.HALF_UP);
```

외부 상품 가격에 1,000을 곱하고 원 단위로 반올림하여 저장합니다.

이는 실제 환율이 아닌 과제에서 정한 고정 환산 규칙입니다.

### 2.3. 장바구니 관리

- 위시리스트 상품을 장바구니에 등록
- 동일 상품 추가 시 기존 수량에 합산
- 상품별 수량 1~5개 제한
- 장바구니 최대 20종류 제한
- 외부 상품의 저장된 재고 수량 초과 방지
- 수동 등록 상품은 재고 제한 없음
- 장바구니 전체 조회
- 장바구니 상품 수량 변경
- 장바구니 상품 삭제
- JDBC를 통한 장바구니 데이터 저장 및 조회

**장바구니 스냅샷**

상품을 장바구니에 처음 등록할 때 상품 이름, 가격, 재고를 복사하여 저장합니다.

이후 원본 위시리스트 상품 정보가 변경되더라도 장바구니에는 최초 등록 당시의 상품 정보를 유지하도록 설계했습니다.

### 2.4. 쿠폰 및 배송비 계산

| 쿠폰 | 할인 조건 |
|---|---|
| `NONE` | 할인 없음 |
| `PERCENT10` | 상품 합계의 10% 할인, 최대 20,000원 |
| `FIXED15000` | 상품 합계가 100,000원 이상이면 15,000원 할인 |

배송비는 쿠폰 적용 후 상품 금액을 기준으로 계산합니다.

| 할인 후 상품 금액 | 배송비 |
|---|---|
| 100,000원 이상 | 무료 |
| 100,000원 미만 | 5,000원 |
| 빈 장바구니 | 0원 |

금액 미리보기는 장바구니 데이터를 변경하지 않습니다.

---

## 3. REST API

### 3.1. 위시리스트 API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/collections` | 상품 직접 등록 |
| GET | `/collections` | 전체 상품 조회 |
| GET | `/collections/{id}` | 상품 단건 조회 |
| GET | `/collections/recommend?budget=50000` | 예산별 상품 추천 |
| POST | `/collections/import` | 외부 상품 가져오기 |

**상품 직접 등록**

```http
POST /collections
Content-Type: application/json
```

```json
{
  "name": "복싱 글러브",
  "price": 50000
}
```

**외부 상품 등록**

```http
POST /collections/import
Content-Type: application/json
```

```json
{
  "externalId": 1
}
```

### 3.2. 외부 상품 API

| Method | Endpoint | 설명 |
|---|---|---|
| GET | `/external/products?limit=10&skip=0` | 상품 목록 조회 |
| GET | `/external/products/search?q=phone` | 상품 검색 |
| GET | `/external/products/{id}` | 상품 상세 조회 |

조회 조건:

- `limit`: 기본값 10, 허용 범위 1~20
- `skip`: 기본값 0, 0 이상
- `q`: 필수 검색어, 공백만 입력 불가

외부 API에서 `id`, `title`, `price`, `category`, `thumbnail`, `stock` 필드를 사용합니다.

### 3.3. 장바구니 API

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/cart/items` | 장바구니 상품 추가 |
| GET | `/cart` | 장바구니 전체 조회 |
| PUT | `/cart/items/{id}` | 장바구니 수량 변경 |
| DELETE | `/cart/items/{id}` | 장바구니 상품 삭제 |
| GET | `/cart/preview?coupon=NONE` | 쿠폰 및 배송비 계산 |

**장바구니 상품 추가**

```http
POST /cart/items
Content-Type: application/json
```

```json
{
  "collectionItemId": 1,
  "quantity": 2
}
```

**장바구니 수량 변경**

```http
PUT /cart/items/1
Content-Type: application/json
```

```json
{
  "quantity": 3
}
```

---

## 4. JDBC 기반 데이터베이스 연동 및 MySQL 전환

### 4.1. 변경 목적

기존에는 `ArrayList`를 이용하여 위시리스트와 장바구니 데이터를 메모리에 저장했습니다.

이 방식은 구현이 간단하지만 애플리케이션이 종료되면 저장된 데이터가 사라지는 문제가 있었습니다.

이를 개선하기 위해 먼저 JDBC와 파일 기반 H2를 도입했으며, 이후 과제 요구사항에 맞춰 애플리케이션 데이터베이스를 MySQL로 전환했습니다.

**변경 전**

```text
Controller
    ↓
Service
    ↓
Repository Interface
    ↓
MemoryCollectionRepository / MemoryCartRepository
    ↓
ArrayList
```

**변경 후**

```text
Controller
    ↓
Service
    ↓
Repository Interface
    ↓
JdbcCollectionRepository / JdbcCartRepository
    ↓
JdbcTemplate
    ↓
MySQL Database
```

기존 Controller, Service, Domain, DTO의 역할과 비즈니스 로직은 최대한 유지하고 Repository 구현체를 교체했습니다.

### 4.2. JDBC 및 데이터베이스 의존성

`build.gradle`에서는 JDBC와 MySQL 드라이버를 사용합니다. H2는 테스트 실행을 위해 유지합니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-jdbc'
runtimeOnly 'com.mysql:mysql-connector-j'
testRuntimeOnly 'com.h2database:h2'
```

> 위 코드는 필요한 의존성을 설명한 예시입니다. 실제 프로젝트의 H2 의존성 구성은 `build.gradle`을 기준으로 확인합니다.

JPA를 사용하지 않고 `JdbcTemplate`으로 SQL을 직접 실행합니다. Repository 인터페이스와 서비스 로직은 유지한 채 데이터베이스 연결 설정 및 DB별 SQL 문법을 변경했습니다.

### 4.3. MySQL 연결 설정

먼저 MySQL에 데이터베이스를 생성합니다.

```sql
CREATE DATABASE IF NOT EXISTS wishlist_budget
CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;
```

`src/main/resources/application.properties`:

```properties
spring.application.name=wishlist-budget
spring.datasource.url=jdbc:mysql://localhost:3306/wishlist_budget
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
spring.datasource.username=${DB_USERNAME:root}
spring.datasource.password=${DB_PASSWORD}
spring.sql.init.mode=always
spring.sql.init.schema-locations=classpath:schema.sql
```

`DB_PASSWORD`는 환경변수로 전달하며 실제 비밀번호를 Git에 커밋하지 않습니다. IntelliJ에서는 `WishlistBudgetApplication`의 **Run/Debug Configurations → Environment variables**에서 설정할 수 있습니다.

```text
DB_USERNAME=root
DB_PASSWORD=<본인의 MySQL 비밀번호>
```

`schema.sql`에는 `CREATE TABLE IF NOT EXISTS`를 사용해 테이블이 없을 때만 생성하도록 했습니다. MySQL의 자동 증가 기본 키에는 `AUTO_INCREMENT`를 사용합니다.

테스트에서는 `src/test/resources/application.properties` 및 `schema.sql`을 통해 H2 데이터베이스를 별도로 사용합니다. 따라서 MySQL 연결 없이 실행할 수 있는 테스트와 실제 MySQL 연결 검증을 구분해야 합니다.

**참고:** 기존 H2 파일에 저장된 데이터는 MySQL로 자동 이전되지 않습니다. 기존 데이터가 필요하다면 별도의 마이그레이션 작업이 필요합니다.

### 4.4. 위시리스트 테이블

**`collection_items`**

| 컬럼 | 자료형 | 설명 |
|---|---|---|
| `id` | BIGINT | 기본 키, 자동 생성 |
| `name` | VARCHAR(255) | 상품 이름 |
| `price` | INTEGER | 상품 가격 |
| `source_type` | VARCHAR(20) | MANUAL / EXTERNAL |
| `external_id` | BIGINT | 외부 상품 ID |
| `stock` | INTEGER | 외부 상품 재고 |

테이블 생성 SQL:

```sql
CREATE TABLE IF NOT EXISTS collection_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    name VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    source_type VARCHAR(20) NOT NULL,
    external_id BIGINT,
    stock INTEGER,

    CONSTRAINT uk_collection_external
        UNIQUE (source_type, external_id)
);
```

**설계 의도**

- `id`는 MySQL 데이터베이스의 `AUTO_INCREMENT`로 자동 생성합니다.
- `source_type`으로 수동 등록 상품과 외부 상품을 구분합니다.
- `external_id`는 외부 상품 ID를 저장합니다.
- `stock`은 외부 상품의 재고 정보를 저장합니다.
- 외부 상품의 중복 등록을 방지하기 위해 UNIQUE 제약조건을 사용했습니다.
- 수동 등록 상품은 `external_id`가 NULL이므로 동일한 이름의 상품도 등록할 수 있습니다.

### 4.5. 장바구니 테이블

**`cart_items`**

| 컬럼 | 자료형 | 설명 |
|---|---|---|
| `id` | BIGINT | 기본 키, 자동 생성 |
| `collection_item_id` | BIGINT | 원본 위시리스트 상품 ID |
| `name` | VARCHAR(255) | 최초 등록한 상품 이름 |
| `price` | INTEGER | 최초 등록한 상품 가격 |
| `stock` | INTEGER | 최초 등록한 재고 |
| `quantity` | INTEGER | 장바구니 수량 |

테이블 생성 SQL:

```sql
CREATE TABLE IF NOT EXISTS cart_items (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    collection_item_id BIGINT NOT NULL,
    name VARCHAR(255) NOT NULL,
    price INTEGER NOT NULL,
    stock INTEGER,
    quantity INTEGER NOT NULL
);
```

장바구니에는 원본 위시리스트 상품 ID와 상품 정보 복사본을 함께 저장합니다.

원본 위시리스트 상품이 변경되더라도 장바구니의 가격과 재고가 자동으로 변경되지 않도록 설계했습니다.

### 4.6. JDBC Repository 구현

**`JdbcCollectionRepository`**

- 상품 직접 등록
- 외부 상품 등록
- 전체 상품 조회
- 상품 ID 조회
- 외부 상품 중복 여부 확인
- DB에서 생성한 상품 ID 반환

**`JdbcCartRepository`**

- 장바구니 상품 등록
- 장바구니 ID 조회
- 위시리스트 상품 ID로 조회
- 장바구니 전체 조회
- 장바구니 수량 변경
- 장바구니 상품 삭제

기존 Repository 인터페이스를 유지하고 JDBC 구현체를 추가했습니다.

실제 애플리케이션에서는 JDBC Repository를 사용하고, 기존 메모리 Repository는 단위 테스트에서 활용할 수 있도록 유지했습니다.

### 4.7. SQL 작성 및 정렬 정책

데이터 저장과 조회에는 SQL을 사용합니다.

**상품 등록**

```sql
INSERT INTO collection_items
(name, price, source_type, external_id, stock)
VALUES (?, ?, ?, ?, ?);
```

**전체 상품 조회**

```sql
SELECT id, name, price, source_type, external_id, stock
FROM collection_items
ORDER BY id ASC;
```

**상품 ID 조회**

```sql
SELECT id, name, price, source_type, external_id, stock
FROM collection_items
WHERE id = ?;
```

**외부 상품 중복 확인**

```sql
SELECT COUNT(*)
FROM collection_items
WHERE source_type = ?
  AND external_id = ?;
```

SQL에 사용자 입력값을 직접 문자열로 연결하지 않고 바인딩 파라미터를 사용했습니다.

목록 조회 시 `ORDER BY id ASC`를 사용하여 등록 순서를 유지하도록 했습니다.

---

## 5. 예외 처리

| HTTP 상태 코드 | 발생 상황 |
|---|---|
| `400 Bad Request` | 잘못된 입력값, 수량, 쿠폰 |
| `404 Not Found` | 존재하지 않는 위시리스트·장바구니·외부 상품 |
| `409 Conflict` | 외부 상품 중복 등록, 수량·재고 제한 위반 |
| `502 Bad Gateway` | 외부 API 오류 및 연결 실패 |

외부 상품 중복 등록은 서비스에서 확인하는 것뿐 아니라 데이터베이스의 UNIQUE 제약조건으로도 방지하도록 설계했습니다.

외부 API 요청에 실패한 경우 불완전한 상품 정보를 데이터베이스에 저장하지 않도록 처리했습니다.

---

## 6. 테스트 및 검증

### 6.1. 서비스 테스트

JUnit 5를 이용하여 위시리스트와 장바구니의 주요 비즈니스 로직을 검증했습니다.

- 상품 정상 등록 및 조회
- 예산별 상품 추천
- 외부 상품 정상 등록
- 외부 상품 중복 등록 방지
- 품절 상품 위시리스트 등록
- 장바구니 수량 합산
- 수량 및 재고 제한
- 장바구니 수정·삭제
- 쿠폰 할인 및 배송비 계산

### 6.2. 외부 API 테스트

외부 DummyJSON 서버 대신 테스트용 로컬 HTTP 서버를 사용하여 외부 API 응답을 검증했습니다.

- 정상 응답 처리
- 404 오류 처리
- 외부 서버 500 오류 처리
- HTTP 201·400·404·409·502 응답 검증

### 6.3. JDBC Repository 테스트

JDBC Repository를 대상으로 다음 동작을 검증했습니다.

- 위시리스트 데이터 저장 및 조회
- DB 자동 생성 ID 확인
- 외부 상품 중복 등록 방지
- DB에 저장된 데이터의 객체 복원

### 6.4. 통합 테스트 DB 분리

JDBC 전환 이후 기존 HTTP 통합 테스트에서 다음 두 테스트가 실패했습니다.

```text
ExternalApiIntegrationTest
- importProduct_success()
- importProduct_duplicate()
```

기존에는 메모리 Repository를 사용했기 때문에 테스트 데이터가 초기화됐지만, 파일 기반 H2로 전환한 이후에는 테스트 실행 사이에 데이터가 남을 수 있었습니다.

이를 해결하기 위해 통합 테스트에서 별도의 H2 메모리 데이터베이스를 사용하도록 설정했습니다.

```java
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:integration_test;DB_CLOSE_DELAY=-1"
})
```

실제 애플리케이션의 MySQL DB와 테스트용 H2 DB를 분리하여 기존 데이터의 영향을 받지 않도록 개선했습니다.

### 6.5. 서버 재시작 후 데이터 유지 검증 (기존 H2 단계)

MySQL 전환 이전에 파일 기반 H2의 데이터 유지 여부를 직접 확인했습니다. 아래는 당시 검증 기록입니다.

**검증 순서**

1. Spring Boot 실행
2. 위시리스트에 상품 등록
3. 등록된 상품의 ID, 이름, 가격 확인
4. 장바구니에 상품 추가
5. 애플리케이션 종료
6. 동일한 설정으로 Spring Boot 재실행
7. 위시리스트 및 장바구니 데이터 재조회

**검증 결과**

당시 파일 기반 H2 환경에서 서버를 종료한 뒤 다시 실행해도 기존 위시리스트와 장바구니 데이터가 유지되는 것을 확인했습니다. MySQL 환경에서도 동일한 재시작 검증을 수행해 확인할 수 있습니다.

### 6.6. 최종 테스트 및 빌드

테스트 실행:

```bash
./gradlew clean test
```

결과:

```text
BUILD SUCCESSFUL
```

전체 빌드:

```bash
./gradlew clean build
```

결과:

```text
BUILD SUCCESSFUL
```

기존 테스트 실패를 수정한 후 H2 기반 단계에서 전체 테스트와 빌드가 정상적으로 완료됐습니다. MySQL 전환 후에는 동일 명령어로 재검증할 수 있습니다.

---

## 7. 실행 방법

### 7.1. 실행 환경

- Java 21
- Gradle Wrapper
- MySQL 8.4.8 실행 및 `wishlist_budget` 데이터베이스 생성
- `DB_USERNAME`, `DB_PASSWORD` 환경변수 설정
- 인터넷 연결 (DummyJSON 호출 시 필요)

### 7.2. 서버 실행

```bash
./gradlew bootRun
```

기본 접속 주소:

```text
http://localhost:8080
```

### 7.3. 프론트엔드 실행

Spring Boot의 정적 리소스 기능을 사용합니다.

```text
src/main/resources/static/
├── index.html
├── style.css
└── script.js
```

별도의 프론트엔드 개발 서버 없이 Spring Boot 실행 후 브라우저에서 접속할 수 있습니다.

### 7.4. 테스트 실행

```bash
./gradlew clean test
```

### 7.5. 전체 빌드

```bash
./gradlew clean build
```

---

## 8. 구현 과정에서 해결한 문제

### 8.1. RestClient.Builder 주입 오류

Spring Boot 4 환경에서 외부 API 연동을 구현하는 과정에서 다음 오류가 발생했습니다.

```text
No qualifying bean of type
'org.springframework.web.client.RestClient$Builder' available
```

`build.gradle`에 RestClient 스타터 의존성을 추가하여 해결했습니다.

```gradle
implementation 'org.springframework.boot:spring-boot-starter-restclient'
```

### 8.2. 외부 상품 ID와 내부 상품 ID 구분

외부 API의 상품 ID와 위시리스트의 내부 ID를 동일하게 사용하면 상품 식별자가 충돌할 수 있습니다.

이를 방지하기 위해 내부 `id`와 `sourceType`, `externalId`를 분리했습니다.

### 8.3. 메모리 저장소의 데이터 소실 문제

기존 `ArrayList` 기반 Repository는 서버 종료 시 데이터가 사라졌습니다.

JDBC와 파일 기반 H2를 도입해 데이터 영속성을 구현했고, 이후 애플리케이션 저장소를 MySQL로 전환했습니다.

### 8.4. 기존 비즈니스 로직 유지

Repository 인터페이스를 유지한 상태에서 JDBC 구현체를 추가했습니다.

이를 통해 Controller와 Service의 기존 역할을 변경하지 않고 데이터 저장 방식만 교체할 수 있었습니다.

### 8.5. 테스트 데이터 충돌 문제

파일 기반 H2 도입 후 기존 테스트에서 저장된 외부 상품이 중복 등록되는 문제가 발생했습니다.

실제 애플리케이션 DB와 테스트 DB를 분리하여 해결했습니다.

### 8.6. H2에서 MySQL로 전환하면서 발생한 접속 문제

MySQL 전환 후 Spring Boot 실행 시 `Access denied for user 'root'@'localhost'` 오류가 발생했습니다. MySQL 계정 인증 문제로 인해 데이터베이스 초기화가 실패했고, 이 영향으로 `JdbcTemplate`과 Repository, Service 빈 생성까지 실패했습니다.

MySQL `root` 비밀번호를 재설정하고 서버를 정상 모드로 재시작한 뒤, `wishlist_budget` 데이터베이스를 생성했습니다. IntelliJ 실행 구성에 `DB_USERNAME`, `DB_PASSWORD` 환경변수를 설정하여 접속 정보를 전달했습니다.

이 오류는 `CartService`의 비즈니스 로직 문제가 아니라 **DataSource 초기화 단계의 MySQL 인증 문제**였습니다.

### 8.7. 상품 카드 이미지 크기 및 클릭 영역 문제

외부 상품 검색 화면에서 상품 이미지가 카드 영역을 벗어나 상품명, 가격, 위시리스트 등록 버튼을 가리는 문제가 발생했습니다.

`style.css`에서 이미지가 카드의 정해진 영역 안에 표시되도록 크기와 배치를 조정하고, 이미지가 클릭 영역을 가리지 않도록 수정했습니다. 기존 HTML 구조와 위시리스트 등록 JavaScript 로직은 유지했습니다.

### 8.8. 장바구니 데이터 스냅샷 유지

장바구니에 상품을 처음 추가할 때 상품 이름, 가격, 재고를 별도로 저장했습니다.

JDBC 전환 이후에도 기존 장바구니 스냅샷 정책을 유지하도록 구현했습니다.

---

## 9. 구현 범위 및 향후 개선 사항

### 구현 완료

- 위시리스트 등록·조회·예산 추천
- 외부 상품 목록·검색·상세 조회
- 외부 상품 위시리스트 등록
- 외부 상품 중복 등록 방지
- 장바구니 등록·조회·수량 변경·삭제
- 쿠폰 할인 및 배송비 계산
- JDBC 기반 위시리스트 저장
- JDBC 기반 장바구니 저장
- H2 파일 기반 영속성 구현 및 검증 (이전 단계)
- 애플리케이션 데이터베이스 MySQL 8.4.8 전환
- MySQL 자동 증가 ID 및 테이블 생성 SQL 적용
- 테스트용 H2 데이터베이스 분리
- 상품 카드 이미지 CSS 개선
- DB 자동 생성 ID 사용
- SQL을 이용한 저장 및 조회
- 서비스 테스트 및 HTTP 통합 테스트
- 테스트 전용 DB 분리
- Gradle 테스트 및 전체 빌드 검증 (H2 단계)