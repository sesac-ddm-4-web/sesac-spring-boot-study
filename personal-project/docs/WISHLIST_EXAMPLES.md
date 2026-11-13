# API 예시 (위시리스트)

위시리스트 API의 요청/응답 예시와 실행 화면입니다. API 요약은 [README](../README.md)를 참고하세요.

요청 본문(JSON):

```json
{ "name": "연필", "price": 500 }
```

## 정상 응답

### 아이템 등록 `POST /wishlist`

![아이템 등록](images/02-api-create-201.png)

이름 앞뒤 공백은 제거되어 저장됩니다.

![이름 공백 제거](images/10-api-name-trim-201.png)

### 전체 목록 조회 `GET /wishlist`

![전체 목록 조회](images/03-api-list-200.png)

### 단건 조회 `GET /wishlist/{id}`

![단건 조회](images/04-api-get-by-id-200.png)

### 예산별 추천 `GET /wishlist?budget=5000`

![예산별 추천](images/05-api-recommend-budget-200.png)

## 오류 응답

**404 - 존재하지 않는 아이템**

![404 Not Found](images/06-error-not-found-404.png)

**400 - 파라미터 타입 불일치** (`GET /wishlist/A`)

![400 Type Mismatch](images/07-error-type-mismatch-400.png)

**400 - 잘못된 가격** (`price: -500`)

![400 Invalid Argument](images/08-error-invalid-price-400.png)

**400 - 요청 값 검증 실패** (`name` 누락)

![400 Validation Failed](images/09-error-validation-failed-400.png)
