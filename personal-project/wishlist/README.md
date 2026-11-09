# 취미 위시리스트와 예산 추천

사고 싶은 물건을 이름과 가격으로 등록하고, 예산을 넣으면 그 안에서 살 수 있는 물건을 싼 순서로 최대 3개 추천하는 API와 화면입니다.

## 환경

| 항목 | 버전 |
|---|---|
| Java | 21 |
| Spring Boot | 4.1.2 (spring-boot-starter-webmvc) |
| 빌드 | Gradle 9.8.2 (Wrapper 포함) |
| 테스트 | JUnit 5 |
| 화면 | 순수 HTML·CSS·JavaScript (외부 라이브러리·CDN 없음) |

## 실행과 테스트

`personal-project/wishlist` 폴더에서 실행합니다.

```bash
./gradlew bootRun   # 서버 실행 → http://localhost:8080
./gradlew test      # 단위 테스트 실행
```

Windows 명령 프롬프트에서는 `gradlew.bat bootRun`, `gradlew.bat test`를 사용합니다.
서버를 실행한 뒤 브라우저에서 `http://localhost:8080/wish.html`을 열면 화면이 나옵니다.

## 구조

```
src/main/java/com/example/wishlist
├─ domain/          CollectionItem, CollectionItemRepository(인터페이스), ItemNotFoundException
├─ infrastructure/  ListCollectionItemRepository (List에 저장하는 메모리 구현)
├─ application/     WishService (검증, 조회, 추천)
└─ presentation/    WishController (API, 예외 → HTTP 상태 코드)
src/main/resources/static/wish.html    화면
```

- Repository는 인터페이스(`CollectionItemRepository`)와 메모리 구현(`ListCollectionItemRepository`)으로 나눴습니다.
- `WishService`는 생성자로 `CollectionItemRepository`를 받습니다. 테스트에서는 `new WishService(new ListCollectionItemRepository())`로 Spring 없이 만듭니다.
- 데이터는 메모리에만 있어서 서버를 다시 시작하면 비워지고 id도 1부터 다시 시작합니다.

## 규칙

| 대상 | 규칙 |
|---|---|
| 이름 | `null`, 빈 문자열, 공백만 있는 이름은 거부. 앞뒤 공백은 제거해서 저장 |
| 가격 | 0 이상의 정수. 무료 아이템(0원) 허용 |
| id | 저장할 때 프로그램이 1부터 차례로 부여. 등록 응답에 담긴 실제 id로 조회 |
| 없는 id | `ItemNotFoundException`으로 구분 → 404 |
| 실패한 등록 | 검증을 모두 통과한 뒤에만 저장하므로 목록과 다음 id가 바뀌지 않음 |
| 예산 | 0 이상. 음수는 거부 |
| 추천 | 개별 가격이 예산 이하인 물건만, 가격 오름차순, 같은 가격은 등록 순서 유지, 최대 3개. 없으면 빈 목록 |
| 원본 보존 | 추천은 저장소가 돌려준 복사본으로 계산해서 원본 목록을 정렬하거나 바꾸지 않음 |

추천은 물건 하나를 산다고 할 때의 후보입니다. 여러 개를 함께 산 합계는 따지지 않습니다.

## API

| 기능 | 요청 | 성공 | 실패 |
|---|---|---|---|
| 등록 | `POST /wish/items` (`name`, `price`) | 200, 등록된 아이템 | 400 |
| 전체 목록 | `GET /wish/items` | 200, 등록 순서 목록 | |
| 단건 조회 | `GET /wish/items/{id}` | 200, 아이템 | 404 |
| 추천 | `GET /wish/recommendations?budget=...` | 200, 최대 3개 | 400 |

`name`, `price`, `budget`은 쿼리 파라미터(또는 form 값)로 보냅니다. 숫자 자리에 숫자가 아닌 값이 오면 400입니다.

## 완료한 기능

- [x] 위시리스트에 아이템 등록
- [x] 전체 목록 조회
- [x] 등록 결과의 실제 id로 단건 조회, 없는 id는 404
- [x] 예산 안에서 살 수 있는 아이템 최대 3개 추천
- [x] Repository 인터페이스와 메모리 구현 분리, Service 생성자 주입
- [x] 단위 테스트 5개
- [x] 화면: 등록 폼, 전체 목록, 예산 입력, 추천 결과

## 실제 결과

### API (서버를 실행해 curl로 요청한 결과)

```
POST /wish/items  name=다이어리  price=9000          → 200 {"name":"다이어리","price":9000,"id":1}
POST /wish/items  name="  만년필 "  price=85000      → 200 {"name":"만년필","price":85000,"id":2}
POST /wish/items  name=스티커  price=0               → 200 {"name":"스티커","price":0,"id":3}
POST /wish/items  name=펜  price=1500                → 200 {"name":"펜","price":1500,"id":4}
POST /wish/items  name=마스킹 테이프  price=1500     → 200 {"name":"마스킹 테이프","price":1500,"id":5}
POST /wish/items  name="   "  price=1000             → 400 이름을 입력해 주세요.
POST /wish/items  name=다이어리  price=-1            → 400 가격은 0원 이상이어야 합니다.

GET /wish/items        → 200 [다이어리 9000 (id 1), 만년필 85000 (id 2), 스티커 0 (id 3), 펜 1500 (id 4), 마스킹 테이프 1500 (id 5)]
GET /wish/items/2      → 200 {"name":"만년필","price":85000,"id":2}
GET /wish/items/99     → 404 해당 id의 아이템을 찾을 수 없습니다. (id: 99)

GET /wish/recommendations?budget=10000  → 200 [스티커 0, 펜 1500, 마스킹 테이프 1500]
GET /wish/recommendations?budget=1500   → 200 [스티커 0, 펜 1500, 마스킹 테이프 1500]
GET /wish/recommendations?budget=1499   → 200 [스티커 0]
GET /wish/recommendations?budget=-1     → 400 예산은 0원 이상이어야 합니다.
```

- 실패한 등록 두 번은 목록에 남지 않았고, 다음 등록의 id도 건너뛰지 않았습니다.
- 예산 10000에서 다이어리(9000)도 예산 이하지만, 더 싼 3개가 먼저 채워져 빠졌습니다.
- 같은 1500원인 펜과 마스킹 테이프는 먼저 등록한 펜이 앞에 옵니다.
- 예산 1500에서는 1500원짜리가 포함되고, 1499에서는 빠집니다 (경계).

### 단위 테스트 (`./gradlew test`)

`WishServiceTest` 5개 모두 통과했습니다.

| 테스트 | 확인 내용 |
|---|---|
| 정상 등록 | id가 부여되고 이름 앞뒤 공백이 제거됨 |
| 빈 이름 거부 | `null`, `""`, `"   "` 모두 `IllegalArgumentException`, 목록은 그대로 |
| 음수 가격 거부 | 거부 후 목록과 다음 id가 바뀌지 않음 |
| 음수 예산 거부 | `-1`은 `IllegalArgumentException` |
| 예산 경계 | 10000원짜리는 예산 10000이면 포함, 9999면 제외 |

### 화면

브라우저에서 등록, 목록, 추천을 직접 조작해 확인했습니다.

- 잘못된 입력은 서버가 보낸 메시지를 그대로 보여주고 목록은 바뀌지 않습니다.
- 추천된 물건은 목록과 추천 결과에 같은 점으로 표시됩니다.
- 예산을 넣은 상태에서 물건을 추가하면 추천이 다시 계산됩니다.
- 모바일 폭(390px)에서도 가로 스크롤 없이 한 줄로 배치됩니다.
