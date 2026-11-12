# 취미 위시리스트와 예산 추천

Spring Boot 기반으로 취미 활동에 필요한 상품을 등록하고, 사용자의 예산에 맞는 상품을 추천하는 웹 서비스입니다.

Controller-Service-Repository 계층 구조를 적용하고, 순수 HTML·CSS·JavaScript로 작성한 프론트엔드와 REST API를 연동했습니다.

## 1. 개발 환경

| 구분 | 기술 |
|---|---|
| Language | Java 21 |
| Framework | Spring Boot 4.1.2 |
| Build Tool | Gradle |
| Frontend | HTML, CSS, JavaScript |
| API 통신 | Fetch API |
| 데이터 저장 | ArrayList 기반 In-Memory Repository |
| Test | JUnit 5, Spring Boot Test |

외부 프론트엔드 라이브러리 및 CDN을 사용하지 않았습니다.

## 2. 주요 기능

### 상품 등록

- 상품명과 가격을 입력하여 상품 등록
- `AtomicLong`을 활용한 상품 ID 자동 생성
- 등록한 상품을 메모리 저장소에 저장

### 상품 조회

- 전체 상품 목록 조회
- 상품 ID를 통한 상세 조회
- Stream API를 활용한 ID 검색

### 예산별 상품 추천

- 사용자가 입력한 예산 이하의 상품 필터링
- 가격 오름차순 정렬
- 최대 3개의 상품 추천
- 음수 예산 입력 시 예외 발생

### 프론트엔드

- 상품 등록 폼
- 전체 상품 목록 및 상세 정보 표시
- 예산 입력 및 추천 결과 표시
- `fetch()`를 이용한 REST API 연동

## 3. 프로젝트 구조

```text
personal-project/
├── src/main/java/com/example/personal_project/
│   ├── application/                 # 작업 조율
│   │   └── port/ProductClient.java   # 외부 조회 인터페이스
│   ├── presentation/
│   │   ├── controller/              # HTTP 요청 처리
│   │   └── dto/{item,product,cart}/  # HTTP 요청·응답 DTO
│   ├── domain/
│   │   ├── item/{model,repository}/ # 위시리스트 모델·저장소 인터페이스
│   │   ├── product/model/           # 외부 상품 모델·가격 환산
│   │   └── cart/{model,repository}/ # 장바구니 모델·규칙·저장소 인터페이스
│   └── infrastructure/
│       ├── repository/              # 메모리 저장소 구현
│       └── client/                  # 외부 Client와 전송 DTO
├── src/main/resources/static/index.html
├── src/test/java/
├── build.gradle
└── README.md
```

외부 상품·가져오기·장바구니는 단계별로 구현 중입니다. 장바구니 추가·조회·수량 변경·삭제는 연결했으며, 쿠폰·배송비 미리보기도 구현했습니다. 기존 `/items`와 `/recommend` API는 유지합니다. 파일별 구현 순서와 검증 항목은 [리팩토링 후 구현 안내](../docs/guide/refactoring-todo.md)를 참고합니다.

## 4. API 명세

| Method | Endpoint | 설명 |
|---|---|---|
| POST | `/items` | 상품 등록 |
| GET | `/items` | 전체 상품 조회 |
| GET | `/items/{id}` | 상품 상세 조회 |
| GET | `/recommend?budget=30000` | 예산별 추천 |

### 상품 등록 요청 예시

```http
POST /items
Content-Type: application/json
```

```json
{
  "name": "키보드",
  "price": 25000
}
```

성공 응답:

```text
등록
```

### 예산별 추천 예시

```http
GET /recommend?budget=30000
```

예산 이하 상품 중 가격이 낮은 순서대로 최대 3개를 반환합니다.

## 5. 실행 방법

프로젝트 디렉터리로 이동합니다.

```bash
cd personal-project
```

Spring Boot 애플리케이션을 실행합니다.

Windows:

```powershell
.\gradlew.bat bootRun
```

macOS / Linux:

```bash
./gradlew bootRun
```

실행 후 브라우저에서 접속합니다.

```text
http://localhost:8080
```

## 6. 테스트

Windows:

```powershell
.\gradlew.bat test
```

macOS / Linux:

```bash
./gradlew test
```

### 테스트 및 디버깅 내용

**1. Repository 메서드 호출 오류**

- 원인: `ItemRepository.add()`를 정적 메서드처럼 호출
- 해결: 생성자 주입된 `itemRepository.add()`로 변경

**2. 상품 ID 조회 오류**

- 원인: 상품 ID를 `ArrayList`의 인덱스로 사용하여 `IndexOutOfBoundsException` 발생
- 해결: Stream API로 상품의 실제 ID를 비교하도록 변경

### 최종 검증 결과

| 검증 항목 | 결과    |
|---|-------|
| 상품 등록 | 확인 완료 |
| 전체 상품 조회 | 확인 완료 |
| 상품 ID 조회 | 확인 완료 |
| 예산별 추천 | 확인 완료 |
| 프론트엔드 API 연동 | 확인 완료 |
| `./gradlew test` | 확인 완료 |

## 7. 구현 시 고려 사항

- **계층 분리:** Controller는 HTTP 요청 처리, Service는 비즈니스 로직, Repository는 데이터 저장 및 조회를 담당하도록 구성했습니다.
- **의존성 주입:** `ItemRepository` 인터페이스를 사용하여 저장소 구현에 대한 의존성을 줄였습니다.
- **Stream API:** 예산 필터링, 가격 정렬, 최대 3개 제한을 선언적으로 구현했습니다.
- **메모리 저장소:** 데이터베이스 없이 기능을 구현했으며, 애플리케이션 재시작 시 저장된 데이터는 초기화됩니다.


## 장바구니 API (9단계)

로컬 위시리스트의 `itemId`로 담고, 반환된 `cartItemId`로 변경·삭제합니다.
외부 API를 다시 조회하지 않고 저장된 재고를 사용합니다.

| Method | Endpoint | 요청 | 성공 응답 |
| --- | --- | --- | --- |
| POST | `/cart/items` | `{"itemId":1,"quantity":2}` | 200, 장바구니 전체 목록 |
| GET | `/cart/items` | 없음 | 200, 장바구니 전체 목록 |
| PATCH | `/cart/items/{cartItemId}` | `{"quantity":3}` | 200, 장바구니 전체 목록 |
| DELETE | `/cart/items/{cartItemId}` | 없음 | 204, 본문 없음 |

목록 응답 예시:

```json
[
  {"cartItemId":1,"itemId":1,"name":"키보드","unitPrice":30000,"quantity":2}
]
```

- id는 양수, 요청 수량은 1~5입니다. 누락·잘못된 형식·범위는 400입니다.
- 없는 위시리스트 상품이나 장바구니 행은 404입니다.
- 같은 상품은 수량을 합산합니다. 합산 후 5개 초과·외부 재고 초과·21번째 상품 추가는 409입니다.
- 수동 상품에는 재고 제한이 없습니다. 품절 외부 상품은 담을 수 없습니다.
- 검증 성공 후에만 저장하며 실패한 요청은 기존 장바구니를 유지합니다.
- 단일 CartService 인스턴스에서 추가·변경·삭제의 조회부터 저장까지 동기화합니다.
- 쿠폰·배송비 미리보기는 아래 10단계 API에서 제공합니다.

장바구니 검증: `CartControllerTest`에서 실제 Service·Domain·메모리 저장소와 MockMvc를 연결해
정상 흐름, 오류 응답, 실패 보존, 저장소 복사본을 확인합니다.


## 금액 미리보기 API (10단계)

`GET /cart/preview?couponCode=PERCENT10`

couponCode를 생략하면 NONE입니다. 빈 문자열·공백·소문자·알 수 없는 코드는 400으로 응답합니다.
쿠폰은 한 번에 하나만 사용하며 미리보기는 장바구니를 저장하거나 변경하지 않습니다.

| 쿠폰 | 규칙 |
| --- | --- |
| NONE | 할인 없음 |
| PERCENT10 | 상품 합계의 10%, 원 미만 버림, 최대 20,000원 |
| FIXED15000 | 상품 합계 100,000원 이상일 때 15,000원 할인 |

배송비는 할인 후 금액이 100,000원 이상이면 0원, 미만이면 5,000원입니다.
빈 장바구니는 NONE만 허용하며 모든 금액이 0입니다. 상품 합계가 0이어도 행이 있으면 기본 배송비가 적용됩니다.
사용할 수 없는 쿠폰은 400이며, long 곱셈·합계 범위 초과도 400으로 거부합니다.

상품 합계 110,000원에 PERCENT10을 적용한 응답:

```json
{
  "subtotal": 110000,
  "discount": 11000,
  "discountedSubtotal": 99000,
  "shipping": 5000,
  "finalAmount": 104000,
  "coupon": "PERCENT10"
}
```

동일 합계에서 NONE 최종 금액은 110,000원, FIXED15000 최종 금액은 100,000원입니다.

책임 분리:

- Coupon: 쿠폰 코드 해석
- CartAmountCalculator: long 금액·할인·배송비 계산
- CartPreview: 계산 결과 값
- CartService: 저장소 조회와 계산 조율, 미리보기에서는 save 호출 없음
- CartPreviewController·CartPreviewResponse: HTTP 입력·응답
- global/exception: 잘못된 쿠폰·금액 범위 초과 예외, 기존 전역 처리기로 400 반환

검증은 CartAmountCalculatorTest의 계산 경계값과 CartPreviewControllerTest의 HTTP 응답·성공/실패 후 데이터 보존·저장 미호출을 포함합니다.
최적 쿠폰 자동 추천은 아래 선택 기능 API로 제공합니다.


## 공통 오류 응답 (11단계)

GlobalExceptionHandler는 업무 오류와 요청 해석 오류를 다음 JSON 형식으로 반환합니다.

```json
{"code":"CART_CONFLICT","message":"상품 재고가 부족합니다."}
```

| HTTP | code | 상황 |
| --- | --- | --- |
| 400 | INVALID_REQUEST | 잘못된 입력, 필수 파라미터 누락, 타입 불일치, 잘못된 JSON |
| 400 | INVALID_COUPON | 알 수 없거나 사용할 수 없는 쿠폰 |
| 400 | CART_AMOUNT_OVERFLOW | long 계산 범위 초과 |
| 404 | ITEM_NOT_FOUND | 없는 로컬 상품 |
| 404 | CART_ITEM_NOT_FOUND | 없는 장바구니 행 |
| 404 | EXTERNAL_PRODUCT_NOT_FOUND | 외부 상세 조회의 404 |
| 409 | DUPLICATE_EXTERNAL_PRODUCT | 같은 외부 상품 재가져오기 |
| 409 | CART_CONFLICT | 수량 합산·재고·상품 종류 제한 충돌 |
| 502 | EXTERNAL_API_FAILURE | 외부 연결 실패·시간 초과·HTTP 오류·응답 변환 실패 |

외부 연결 오류의 원인은 예외에 보존하지만 HTTP 응답에는 내부 원인을 노출하지 않습니다.
외부 상세의 404만 상품 없음으로 구분하고, 외부 목록·검색의 HTTP 실패는 연동 실패로 처리합니다.
외부 응답의 필수 필드 누락·음수 가격/재고/페이지 정보도 502로 처리합니다.
사용자 정의 예외와 오류 DTO는 global/exception, HTTP 변환은 global/exception/handler에서 관리합니다.

검증: GlobalExceptionHandlerTest는 오류 코드·상태·요청 해석 오류를,
DummyJsonClientTest는 가짜 HTTP 응답을 통한 404·500·시간 초과·잘못된 JSON·필수 값 누락과 목록·검색 변환을,
ImportItemServiceTest는 외부 조회 실패 후 기존 위시리스트 보존을 확인합니다.
기존 장바구니 테스트의 추가·변경·미리보기 실패 보존 검증도 유지합니다.


## 외부 상품·장바구니 화면과 최적 쿠폰

기존 `src/main/resources/static/index.html`의 색상·카드·입력창 스타일을 유지하여 확장했습니다.
순수 HTML·CSS·JavaScript와 동일 출처 Spring API를 사용하며 외부 JS/CSS 라이브러리와 CDN은 사용하지 않습니다.
외부 상품 이미지만 상품 응답의 thumbnail URL로 표시합니다.

- 외부 상품: 목록·검색·이전/다음 페이지·상세·위시리스트 가져오기
- 위시리스트: 기존 수동 등록·상세·예산 추천 유지, 장바구니 담기 추가
- 장바구니: 조회·수량 변경·삭제, 변경 후 금액 새로 계산
- 미리보기: 쿠폰 선택·상품 합계·할인·할인 후 금액·배송비·최종 금액 표시
- 최적 쿠폰: `GET /cart/best-coupon`, CartPreviewResponse와 동일한 응답 구조

최적 쿠폰은 NONE과 사용할 수 있는 쿠폰을 배송비 포함 finalAmount로 비교합니다.
동률이면 NONE → PERCENT10 → FIXED15000 순으로 선택하며 장바구니 저장 상태를 변경하지 않습니다.
예: 합계 110,000원은 FIXED15000(최종 100,000원), 합계 150,000원은
PERCENT10과 FIXED15000의 동률에서 PERCENT10을 선택합니다. 빈 장바구니는 NONE·0원입니다.

### 실제 브라우저 QA (2026-11-12)

로컬 Spring 서버를 실행한 뒤 브라우저에서 실제 API 응답이 DOM에 표시되는 것을 확인했습니다.
외부 상품 조회·가져오기는 실제 DummyJSON에 연결했습니다.

| 검증 항목 | 실제 결과 |
| --- | --- |
| 수동 등록·위시리스트 | QA 키보드 110,000원 등록·카드 표시 |
| 외부 목록·검색 | 6개 카드·이미지 표시, phone 검색 총 23개 표시 |
| 외부 상세 | Apple iPhone Charger, 외부 id 104, 가격 19.99, 재고 31 표시 |
| 외부 가져오기 | 로컬 id 2, 19,990원으로 위시리스트 표시 |
| 중복 가져오기 | HTTP 409 메시지 표시, 위시리스트 유지 |
| 기존 예산 추천·상세 | 30,000원 예산에서 가져온 상품 추천·상세 표시 |
| 장바구니 추가·수량 변경 | 1개→2개, 상품 소계 110,000원→220,000원 반영 |
| 외부 상품 담기·삭제 | 19,990원 상품 추가 후 삭제·목록 갱신 |
| 쿠폰 추천 | 합계 110,000원에서 FIXED15000·배송비 5,000원·최종 100,000원 |
| 할인 상한 포함 추천 | 합계 239,990원에서 PERCENT10·할인 20,000원·최종 219,990원 |
| 모바일 레이아웃 | 390px viewport에서 단일 열 배치, 문서 가로 넘침 없음 |

검증 화면: [데스크톱](../docs/qa/cart-desktop.png), [모바일](../docs/qa/cart-mobile.png).
QA용 메모리 상품이 실행 중 서버에 남아 있으며 서버 재시작 시 초기화됩니다.

자동 테스트에는 최적 쿠폰의 배송비 포함 비교·동률 우선순위·빈 장바구니와
최적 쿠폰 API 호출 시 저장 미호출 검증을 추가했습니다.


## 테스트 보충 결과와 로컬 프론트 QA

2026-11-12 기준 `./gradlew.bat clean test --offline` 실행 결과: **81개 통과, 실패 0, 오류 0**.
기존 51개에서 30개를 보충했습니다. 설계 요구와 테스트를 아래처럼 연결합니다.

| 설계 조건 | 자동 검증 위치 |
| --- | --- |
| 기존 수동 등록·예산 추천·실패 보존 | ItemServiceTest, ItemServiceIntegrationTest |
| 외부 목록 기본값·limit/skip·검색어 검증·Client 미호출 | ProductControllerTest |
| 외부 id 응답 계약·404/502 | ProductControllerTest |
| JSON 매핑·불필요 필드 무시·빈 페이지·잘못된 외부 값·통신 실패 | DummyJsonClientTest |
| BigDecimal 환산·HALF_UP·int 초과 금액·범위 초과 | PriceConverterTest |
| 서버가 이름/가격 결정·품절 가져오기·로컬 id 발급·중복 시 재조회 금지 | ItemImportControllerTest |
| 가져오기 입력 오류·404/502·실패 보존 | ItemImportControllerTest, ImportItemServiceTest |
| 수량 합산·재고·20종·삭제·실패 보존 | CartControllerTest, CartTest |
| 20종에서도 기존 행 합산 허용·삭제 후 슬롯 재사용·실패 후 id 유지 | CartTest |
| 저장/조회 복사본·id 발급 상태·원본과 분리된 상품 정보 | CartTest, ListItemRepositoryTest |
| 할인·배송비 경계·할인 상한·최적 쿠폰 동률·빈 장바구니 | CartAmountCalculatorTest |
| 미리보기/최적 쿠폰 저장 미호출·성공/실패 상태 보존 | CartPreviewControllerTest |
| 공통 오류 JSON·파라미터 누락·타입 불일치·잘못된 JSON | GlobalExceptionHandlerTest |

자동 테스트는 실제 DummyJSON을 호출하지 않습니다. 이번 보충 작업은 Java 도메인·Service·HTTP 계약을 검증했으며,
프론트의 DOM 렌더링은 앞선 실제 브라우저 QA와 아래 수동 시나리오로 구분합니다.
수동 상품 수정 API는 현재 구현되지 않아 수정 기능 보존·외부 상품 수정 거부를 완료로 판정하지 않습니다.
원본 삭제/수정 API 없이도 장바구니 상품 정보 복사·삭제 후 재추가 시 현재 값 반영은 도메인 테스트에서 검증합니다.

### 실행

PowerShell에서 실행합니다.

```powershell
cd C:\sesac-spring-boot-study\personal-project
.\gradlew.bat test
.\gradlew.bat bootRun
```

Gradle 의존성이 이미 캐시되어 있으면 `--offline`을 붙여 실행할 수 있습니다.
브라우저에서 `http://localhost:8080/`을 엽니다. index.html 파일을 직접 더블클릭하지 마세요.
다른 서버가 8080을 사용하면 기존 서버를 실행한 터미널에서 Ctrl+C 후 다시 실행합니다.
서버 재시작 시 메모리 위시리스트와 장바구니가 초기화됩니다. 화면 새로고침만으로는 초기화되지 않습니다.
프론트 변경 후 이전 화면이 남으면 Ctrl+F5로 새로고침합니다.

개발자 도구(F12)의 Network에서 Fetch/XHR 필터를 켜면 아래 요청의 Status와 Response를 확인할 수 있습니다.
각 시나리오의 실제 결과는 직접 실행한 뒤 기록하세요.

### 시나리오 1: 빈 장바구니

1. 서버를 새로 시작하고 화면을 엽니다.
2. 장바구니가 비어 있고 상품 합계·할인·배송비·최종 금액이 모두 0인지 확인합니다.
3. 최저 금액 쿠폰 찾기: NONE, 최종 0원이어야 합니다.
4. PERCENT10 또는 FIXED15000 선택: 400 오류 메시지를 표시하고 장바구니는 비어 있어야 합니다.
5. NONE으로 되돌립니다.

### 시나리오 2: 기존 위시리스트와 추천

1. 수동 상품 `QA 키보드`, 가격 `110000`을 등록합니다.
2. 위시리스트 카드와 상세에 110,000원이 표시되는지 확인합니다.
3. 추천 예산 110,000원: 해당 상품이 추천되어야 합니다.
4. 추천 예산 109,999원: 다른 저가 상품을 등록하지 않았다면 결과가 비어 있어야 합니다.
5. 등록/추천/상세 요청이 각각 POST /items, GET /recommend, GET /items/{id}인지 확인합니다.

### 시나리오 3: 쿠폰·배송비 계산

QA 키보드를 장바구니에 1개 담고 비교합니다.

| 선택 | 상품 합계 | 할인 | 할인 후 | 배송비 | 최종 |
| --- | ---: | ---: | ---: | ---: | ---: |
| NONE | 110,000 | 0 | 110,000 | 0 | 110,000 |
| PERCENT10 | 110,000 | 11,000 | 99,000 | 5,000 | 104,000 |
| FIXED15000 | 110,000 | 15,000 | 95,000 | 5,000 | 100,000 |

최저 금액 쿠폰 찾기는 FIXED15000·100,000원을 표시해야 합니다.
GET /cart/preview와 GET /cart/best-coupon을 반복해도 장바구니 행·수량은 그대로여야 합니다.

### 시나리오 4: 수량 변경·합산 실패·삭제

1. 키보드 수량을 2로 변경합니다. 상품 합계 220,000원이어야 합니다.
2. 최저 금액 쿠폰 찾기: PERCENT10, 할인 상한 20,000원, 배송비 0원, 최종 200,000원입니다.
3. 수량을 5로 변경하고 위시리스트에서 같은 상품을 한 번 더 담습니다.
4. POST /cart/items가 409이며 기존 수량 5·행 1개를 유지해야 합니다.
5. 수량 6 입력 후 변경: 화면에서 거부하고 저장 수량은 5를 유지해야 합니다. 이 경우 프론트 검증으로 PATCH가 전송되지 않을 수 있습니다.
6. 삭제하면 DELETE /cart/items/{cartItemId}는 204이며 장바구니가 비어야 합니다.
7. 선택 쿠폰이 빈 장바구니에서 사용 불가하면 오류가 나올 수 있습니다. 최저 금액 쿠폰 찾기로 NONE·0원을 확인합니다.

### 시나리오 5: 동률 우선순위

각 상품을 시험할 때 장바구니의 다른 행은 먼저 삭제합니다.

1. `QA 동률`, 150,000원 상품을 등록해 1개 담습니다.
2. PERCENT10과 FIXED15000의 최종 금액은 모두 135,000원입니다.
3. 최저 금액 쿠폰 찾기는 우선순위에 따라 PERCENT10을 선택해야 합니다.
4. 장바구니를 비우고 `QA 소액`, 9원 상품을 1개 담습니다.
5. NONE과 PERCENT10은 모두 최종 5,009원이며 추천은 NONE이어야 합니다.

### 시나리오 6: 정액 쿠폰 사용 조건

1. 장바구니를 비우고 99,999원 상품을 1개 담습니다.
2. FIXED15000 선택: 400·INVALID_COUPON 오류가 표시되어야 합니다.
3. 장바구니의 상품·수량은 그대로이며 NONE을 선택하면 정상 금액이 다시 보여야 합니다.

### 시나리오 7: 실제 외부 상품 연동

1. 외부 상품 전체 보기에서 카드·이미지·페이지 범위를 확인합니다.
2. 다음→이전으로 이동하고 조회 개수를 바꿔 페이지가 갱신되는지 확인합니다.
3. `phone` 검색 후 상세 보기로 이름·외부 id·가격·재고를 확인합니다.
4. 위시리스트로 가져오기: POST /items/import의 201과 원화 가격 카드가 표시되어야 합니다.
5. 기대 가격은 현재 외부 price × 1,000을 HALF_UP으로 반올림한 값입니다. 외부 데이터가 바뀔 수 있으므로 상품명·재고·총 건수는 고정값으로 가정하지 않습니다.
6. 같은 상품 재가져오기: 409·DUPLICATE_EXTERNAL_PRODUCT 메시지가 표시되며 위시리스트 개수는 증가하지 않아야 합니다.
7. 가져온 상품을 장바구니에 담고 수량 변경·미리보기를 확인합니다. 외부 stock을 넘는 수량은 거부되어야 합니다.
8. `zzzz-no-match-20261112`처럼 결과가 없는 검색어는 200과 빈 목록 안내가 표시되어야 합니다.
9. 검색어 공백은 프론트에서 거부합니다. 전체 목록은 전체 보기 버튼으로 조회합니다.

품절 상품 존재 여부와 외부 통신 실패를 실환경에서 임의로 재현하지 않아도 됩니다.
품절 가져오기 허용/장바구니 거부, 재고 제한, 502는 자동 테스트의 고정 데이터로 검증합니다.

### 시나리오 8: 반응형과 오류 복구

1. 개발자 도구의 기기 모드에서 390px 정도의 폭으로 확인합니다.
2. 카드·수량 입력·쿠폰·최종 금액이 잘리지 않고 가로 스크롤 없이 표시되어야 합니다.
3. 서버를 중지한 뒤 새로고침 버튼을 누르면 연결 실패 안내가 표시되어야 합니다.
4. 서버를 재시작한 뒤 새로고침하면 빈 메모리 상태를 정상 조회해야 합니다.

테스트 HTML 보고서: `personal-project/build/reports/tests/test/index.html`.
