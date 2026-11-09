# 게임 위시리스트 & 예산 추천 서버

사고 싶은 게임을 위시리스트에 등록하고, 예산 안에서 살 수 있는 구매 후보를 최대 3개까지 추천하는 Spring Boot 서버입니다.

## 소개 · 환경

| 항목 | 내용 |
|---|---|
| 프로젝트 | 게임 위시리스트 & 예산 추천 |
| Java | `<java -version 결과, 예: 21>` |
| Spring Boot | `<build.gradle의 org.springframework.boot 플러그인 버전>` |
| 빌드 도구 | Gradle |
| 저장소 | 메모리(`CopyOnWriteArrayList`), 서버 재시작 시 초기화 |
| 프론트엔드 | 순수 HTML · CSS · JavaScript (`src/main/resources/static/index.html`) |

### 구조
```
presentation   ProductController          HTTP 요청/응답, 예외 → 상태 코드 변환
application    SimpleProductService       입력 검증, 등록, 추천 로직
domain         Product, ProductRepository 도메인 모델, 저장소 인터페이스
infrastructure ListProductRepository      메모리 구현체
```
## 완료 기능 · API

| 기능 | 메서드 | 엔드포인트 | 요청 | 성공 응답 | 실패 응답 |
|---|---|---|---|---|---|
| 게임 등록 | `POST` | `/products` | `{"name":"산나비","price":15500}` | 200, 등록된 게임 (id 부여) | 400 |
| 전체 목록 | `GET` | `/products` | - | 200, 등록 순서대로 배열 (없으면 `[]`) | - |
| 단건 조회 | `GET` | `/products/{id}` | Path: id | 200, 해당 게임 | 404 (없는 id) |
| 예산 추천 | `GET` | `/recommend/{budget}` | Path: budget | 200, 예산 이하 · 가격 오름차순 · 최대 3개 | 400 (음수 예산) |

### 규칙
- 이름: null · 빈 문자열 · 공백만 있는 값 거부, 앞뒤 공백 제거
- 가격: 0 이상의 정수, 무료(0원) 허용
- id: 서버에서 1부터 순차 부여
- 추천: 예산 이하만 선택 → 가격 오름차순(같은 가격은 등록 순서 유지) → 최대 3개 → 없으면 빈 목록
- 추천 조회는 원본 목록을 변경하지 않음
- 실패한 등록은 기존 목록에 영향을 주지 않음

### 에러 응답

| 상태 | 조건 | 본문 |
|---|---|---|
| 400 | 이름이 비어 있음 | `이름은 비어 있을 수 없습니다.` |
| 400 | 가격이 음수 | `가격은 0 이상의 정수여야 합니다.` |
| 400 | 예산이 음수 | `예산은 0 이상이어야 합니다.` |
| 404 | 존재하지 않는 id | `<실제 응답 본문>` |