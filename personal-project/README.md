# ⛺ CampKit — 캠핑 장비 관리 서비스

캠핑 장비를 등록하고 관리하며, 외부 상품 검색, 예산별 장비 추천, 장바구니 관리 및 예상 결제 금액 계산 기능을 제공하는 Spring Boot 기반 웹 애플리케이션입니다.

## 1. 프로젝트 개요

| 구분 | 기술 |
|---|---|
| Backend | Java 21, Spring Boot 4.1.2 |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL 8.0 |
| Database Access | Spring JDBC, JdbcTemplate |
| Infrastructure | Docker |
| Build Tool | Gradle |
| External API | DummyJSON Products API |
| HTTP Client | Spring RestClient |

## 2. 주요 기능

### ① 캠핑 장비 관리
- 캠핑 장비 이름과 가격을 입력하여 상품 등록
- 등록된 캠핑 장비 전체 조회 및 상세 조회
- 상품 고유 ID 자동 생성
- 예산 이하의 장비를 가격 오름차순으로 최대 3개 추천

### ② 외부 상품 API 연동
- DummyJSON API를 활용한 상품 목록·검색·상세 조회
- 외부 상품을 캠핑 장비 목록에 저장
- 외부 상품 중복 등록 방지
- 외부 상품 가격 환산 및 재고 정보 관리

### ③ 장바구니 관리
- 등록된 캠핑 장비를 장바구니에 추가
- 장바구니 전체 목록 조회
- 상품 수량 변경 및 삭제
- 동일 상품 추가 시 수량 합산
- 상품별 수량 1~5개 제한 및 최대 20종 관리
- 외부 상품의 재고 초과 및 품절 상품 추가 방지

### ④ 쿠폰 및 예상 결제 금액
- 상품별 가격과 수량에 따른 합계 계산
- 10% 할인 쿠폰 (최대 20,000원)
- 10만 원 이상 구매 시 15,000원 할인
- 할인 후 금액에 따른 배송비 계산
- 최종 예상 결제 금액 제공

### ⑤ MySQL 데이터 관리
- Docker 기반 MySQL 데이터베이스 연동
- Spring JDBC를 활용한 데이터 저장 및 조회
- 캠핑 장비와 장바구니 데이터를 별도 테이블로 관리
- 장바구니 데이터 등록·조회·수정·삭제
- 서버 재시작 이후에도 데이터 유지

## 3. 프로젝트 구조

```text
personal-project/
├── src/
│   ├── main/
│   │   ├── java/com/example/campkit/
│   │   │   ├── CampkitApplication.java
│   │   │   ├── controller/
│   │   │   │   ├── CollectionController.java
│   │   │   │   ├── ExternalProductController.java
│   │   │   │   └── CartController.java
│   │   │   ├── domain/
│   │   │   │   ├── CollectionItem.java
│   │   │   │   ├── SourceType.java
│   │   │   │   ├── CartItem.java
│   │   │   │   └── Coupon.java
│   │   │   ├── repository/
│   │   │   │   ├── CollectionRepository.java
│   │   │   │   ├── MemoryCollectionRepository.java
│   │   │   │   ├── JdbcCollectionRepository.java
│   │   │   │   ├── CartRepository.java
│   │   │   │   ├── MemoryCartRepository.java
│   │   │   │   └── JdbcCartRepository.java
│   │   │   ├── service/
│   │   │   │   ├── CollectionService.java
│   │   │   │   ├── ExternalProductService.java
│   │   │   │   └── CartService.java
│   │   │   ├── external/
│   │   │   │   ├── DummyJsonProduct.java
│   │   │   │   ├── DummyJsonPage.java
│   │   │   │   ├── ExternalProductClient.java
│   │   │   │   └── DummyJsonClient.java
│   │   │   ├── dto/
│   │   │   │   ├── AddCartRequest.java
│   │   │   │   ├── UpdateQuantityRequest.java
│   │   │   │   └── CartPreview.java
│   │   │   └── exception/
│   │   │       ├── NotFoundException.java
│   │   │       ├── ConflictException.java
│   │   │       ├── ExternalApiException.java
│   │   │       └── ApiExceptionHandler.java
│   │   └── resources/
│   │       ├── application.properties
│   │       └── static/
│   │           ├── index.html
│   │           ├── css/style.css
│   │           └── js/app.js
│   └── test/java/com/example/campkit/
│       ├── CollectionServiceTest.java
│       ├── service/
│       │   └── CartServiceTest.java
│       └── integration/
│           └── CampkitApplicationTests.java
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## 4. 시스템 아키텍처

```text
Frontend (HTML, CSS, JavaScript)
               │
               ▼
        REST Controller
               │
               ▼
            Service
        (비즈니스 로직)
               │
               ▼
      Repository Interface
               │
               ▼
       JDBC Repository
         (JdbcTemplate)
               │
               ▼
        Docker + MySQL
```

**설계 특징**
- Controller, Service, Repository 계층별 책임 분리
- Repository 인터페이스를 활용한 의존성 분리
- 메모리 기반 저장소에서 JDBC 기반 영속 저장소로 전환
- DTO 및 도메인 객체를 활용한 입력값 검증
- 전역 예외 처리를 통한 일관된 HTTP 응답

## 5. 데이터베이스 구조

**Database:** `product_management`

| 테이블 | 역할 |
|---|---|
| `collection_items` | 캠핑 장비 정보 관리 |
| `cart_items` | 장바구니 상품 및 수량 관리 |

- `collection_items.id`: 캠핑 장비 고유 ID
- `cart_items.collection_item_id`: 장바구니에 담긴 원본 상품 ID
- 장바구니에 상품명·가격·재고 정보를 복사하여 저장
- JDBC Repository에서 SQL을 활용해 데이터 관리

## 6. REST API

기본 주소: `http://localhost:8080`

### 캠핑 장비 API

| Method | Endpoint | 기능 |
|---|---|---|
| POST | `/api/items` | 캠핑 장비 등록 |
| GET | `/api/items` | 전체 목록 조회 |
| GET | `/api/items/{id}` | 상세 조회 |
| GET | `/api/items/recommend?budget=50000` | 예산별 추천 |

### 외부 상품 API

| Method | Endpoint | 기능 |
|---|---|---|
| GET | `/api/external-products` | 외부 상품 목록 |
| GET | `/api/external-products/search?q=phone` | 외부 상품 검색 |
| GET | `/api/external-products/{id}` | 외부 상품 상세 |
| POST | `/api/items/import/{externalId}` | 외부 상품 가져오기 |

### 장바구니 API

| Method | Endpoint | 기능 |
|---|---|---|
| POST | `/api/cart` | 장바구니 상품 추가 |
| GET | `/api/cart` | 장바구니 전체 조회 |
| PATCH | `/api/cart/{id}` | 상품 수량 변경 |
| DELETE | `/api/cart/{id}` | 장바구니 상품 삭제 |
| GET | `/api/cart/preview?couponCode=NONE` | 예상 결제 금액 조회 |

## 7. 프로젝트 실행

MySQL Docker 컨테이너와 데이터베이스가 준비된 환경에서 Spring Boot 애플리케이션을 실행합니다.

```bash
./gradlew.bat bootRun
```

웹페이지 접속: `http://localhost:8080/`

## 8. 구현 범위

캠핑 장비 등록 및 조회, 외부 상품 API 연동, 예산별 추천, 장바구니 관리, 쿠폰 할인 및 예상 결제 금액 계산 기능을 구현했습니다.

기존 메모리 기반 Repository를 JDBC 기반 Repository로 전환하여 MySQL 데이터 영속성을 적용했습니다.

실제 결제 처리 및 재고 차감 기능은 포함하지 않습니다.