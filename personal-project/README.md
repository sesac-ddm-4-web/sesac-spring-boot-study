# 취미 위시리스트 · 외부 상품 연동과 장바구니 · MySQL 저장

원하는 상품을 직접 등록하거나 DummyJSON에서 가져와 위시리스트에 저장하고, 예산별 추천과 장바구니 금액 미리보기를 제공하는 Spring Boot 프로젝트입니다.

기존 위시리스트·예산 추천·이름 부분 검색을 유지하면서 외부 상품 연동, 장바구니 수량 관리, 쿠폰·배송비 계산을 추가했습니다. 작업 위치는 개인 Fork의 `personal-project/`입니다.

## 개발 환경과 실행

| 항목 | 환경 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.2 |
| 빌드 | Gradle Wrapper 9.8.2, Groovy DSL |
| 테스트 | JUnit Jupiter, Mockito, Spring Boot Test, MockMvc |
| 외부 HTTP 호출 | Spring RestClient |
| 저장 방식 | MySQL 8.0.29(prod), 메모리(default·test) |
| 서버 포트 | 8081 |
| 웹 화면 | HTML·CSS·JavaScript, Fetch API |

저장소의 `personal-project` 폴더에서 실행합니다.

```powershell
.\gradlew.bat bootRun
```

테스트와 빌드 명령입니다. 전체 테스트에는 아래의 테스트용 MySQL DB·계정 준비가 필요합니다.

```powershell
.\gradlew.bat test
.\gradlew.bat build
```

macOS / Linux에서는 `./gradlew bootRun`, `./gradlew test`, `./gradlew build`를 사용합니다. 실행 권한이 없다면 `chmod +x gradlew`를 먼저 실행합니다.

기본 주소는 `http://localhost:8081`이며 [웹 화면](http://localhost:8081/)에서 위시리스트·추천·외부 상품 검색/가져오기·장바구니·쿠폰 미리보기를 사용할 수 있습니다. 모든 화면은 실제 Spring HTTP API에 연결합니다. 외부 프론트엔드 라이브러리나 CDN은 사용하지 않습니다.

`default`·`test`의 메모리 실행에서는 재시작 시 데이터가 초기화됩니다. `prod`는 MySQL에 위시리스트·장바구니를 저장하므로 같은 DB로 재시작하면 복원됩니다. 실제 DummyJSON 조회·가져오기에는 인터넷 연결이 필요하며, 자동 테스트는 실제 외부 서버를 호출하지 않습니다.

## MySQL 연결과 테이블 설계

### 실행 프로필

| 프로필 | ItemRepository / CartRepository | DB 연결 |
|---|---|---|
| default | 메모리 구현체 | 자동 연결 설정 제외 |
| test | 메모리 구현체 | 자동 연결 설정 제외 |
| prod | DB 구현체 | 앱 MySQL wishlist |
| prod + dbtest | DB 구현체 | 테스트 전용 MySQL wishlist_repository_test |

`application-default.properties`와 `application-test.properties`에서 DataSource 자동 설정을 제외합니다. DB Repository는 `@Profile("prod")`, 메모리 Repository는 `@Profile({"default", "test"})`로 선택합니다. DB 통합 테스트만 `prod`와 `dbtest`를 함께 활성화합니다.

현재 앱 연결 설정은 `src/main/resources/application.properties`에 있습니다.

```properties
spring.datasource.url=jdbc:mysql://localhost:3307/wishlist
spring.datasource.username=root
spring.datasource.password=${DB_PASSWORD}
spring.datasource.driver-class-name=com.mysql.cj.jdbc.Driver
```

Spring JDBC와 MySQL 드라이버를 사용하며 JPA로 전환하지 않았습니다. HTTP 경로와 ItemRepository 계약은 유지하고, CartRepository에는 변경을 명시적으로 저장할 `save(Cart)`를 추가했습니다.

### Docker와 앱 실행

Docker Desktop을 실행한 뒤 개인 과제용 컨테이너를 준비합니다. 기존 연습 컨테이너와 이름·포트·볼륨을 구분합니다. 아래 생성 명령은 처음 한 번 사용하며, 이미 있다면 `docker start wishlist-mysql`로 실행합니다.

```powershell
$env:MYSQL_ROOT_PASSWORD = Read-Host "새 MySQL 비밀번호"
docker run --name wishlist-mysql -p 3307:3306 -e MYSQL_ROOT_PASSWORD -e MYSQL_DATABASE=wishlist -v wishlist-mysql-data:/var/lib/mysql -d mysql:8.0.29 --character-set-server=utf8mb4 --collation-server=utf8mb4_general_ci
```

MySQL에 접속합니다. 이 명령은 IntelliJ Terminal의 PowerShell에서 실행하고, 비밀번호는 위에서 정한 값으로 입력합니다.

```powershell
docker exec -it wishlist-mysql mysql --default-character-set=utf8mb4 -u root -p wishlist
```

첫 구성 시 `mysql>`에서 [items.sql](src/main/resources/db/items.sql), [cart.sql](src/main/resources/db/cart.sql)의 SQL을 실행합니다. 파일에 저장하는 것만으로 앱 DB에 자동 실행되지는 않습니다. 기존 데이터가 있는 앱 DB에는 생성문을 다시 실행하거나 테이블을 초기화하지 않습니다.

서버용 PowerShell 탭에서 실행합니다. SQL을 입력하는 `mysql>` 탭과 구분합니다.

```powershell
$env:DB_PASSWORD = Read-Host "MySQL 비밀번호"
.\gradlew.bat --console=plain bootRun --args="--spring.profiles.active=prod"
```

같은 볼륨·DB를 유지하면 앱을 종료했다 다시 실행해도 데이터가 복원됩니다. 연결 비밀번호는 Git에 기록하지 않습니다.

### 테이블·자료형·NULL·제약

| 테이블 | 저장할 값 | 주요 제약과 의미 |
|---|---|---|
| items | id BIGINT, name VARCHAR(255), price INT, source_type VARCHAR(20), external_id BIGINT, stock INT | id는 AUTO_INCREMENT PK, 이름·가격·출처 NOT NULL, 외부 id·재고 NULL 허용 |
| cart_items | id BIGINT, item_id BIGINT, name VARCHAR(255), unit_price BIGINT, source_type VARCHAR(20), stock INT, quantity INT | id PK, item_id UNIQUE, stock만 NULL 허용, 행 번호는 Cart가 부여 |
| cart_state | id BIGINT, next_sequence BIGINT | 하나의 장바구니를 나타내는 id=1, next_sequence>=1 |

- Java Long ↔ BIGINT, int/Integer ↔ INT로 저장합니다. 장바구니 단가와 합계 계산은 long입니다.
- 수동 상품은 `MANUAL`, 외부 id·재고는 NULL입니다. RowMapper의 `getObject(..., Long.class/Integer.class)`로 NULL을 그대로 복원합니다.
- 외부 상품은 `DUMMY_JSON`과 외부 id·재고를 함께 복원합니다. DB가 새로운 로컬 id를 생성하며 외부 id와 구분합니다.
- `UNIQUE(source_type, external_id)`가 외부 상품 중복을 막습니다. NULL 외부 id를 가진 여러 수동 상품과 이름 중복은 허용합니다. 중복 키 예외를 기존 409 처리로 연결합니다.
- 가격·재고의 음수와 장바구니 수량 범위는 CHECK 제약으로도 검사합니다.
- `cart_items.item_id`에는 items를 참조하는 FK를 두지 않았습니다. 원본 삭제를 막거나 장바구니를 연쇄 삭제하지 않고, 복사본이 남도록 설계했습니다.
- `cart_state.next_sequence`를 별도 보관하므로 마지막 행이나 전체 행을 삭제해도 재시작 후 번호를 재사용하지 않습니다.
- 목록·검색·장바구니 조회에 `ORDER BY id ASC`를 명시합니다. 추천은 이 순서의 목록을 가격으로 안정 정렬하여 가격 동률의 등록 순서를 보존합니다.
- 이름 검색은 `LOCATE(CAST(:name AS BINARY), CAST(name AS BINARY)) > 0`으로 대소문자와 `%`, `_`를 기존 contains()처럼 문자 그대로 비교합니다.

### 저장 단위와 트랜잭션

장바구니는 서버에 하나이며 최대 20종입니다. `save(Cart)`는 DB에서 읽은 목록과 현재 Cart를 비교해 바뀐 행만 반영합니다.

| 변경 | 실행 SQL | 유지되는 값 |
|---|---|---|
| 새 상품 담기 | 해당 행 INSERT | 기존 상품 행은 다시 저장하지 않음 |
| 기존 상품 수량 변경·중복 추가 | 해당 id의 quantity만 UPDATE | 처음 복사한 이름·단가·출처·재고 유지 |
| 상품 빼기 | 해당 id만 DELETE | 다른 상품 행 유지 |
| 새 행 번호 부여 | next_sequence UPDATE | 수량 변경·삭제 때는 다음 번호 유지 |
| 변경 없음 | 쓰기 SQL 없음 | 기존 행과 다음 번호 유지 |

기존 장바구니 전체를 삭제한 뒤 다시 INSERT하지 않습니다. 예를 들어 노트 수량만 바꾸면 `UPDATE cart_items SET quantity=:quantity WHERE id=:id`로 해당 행만 수정합니다. Cart와 Service의 기존 역할 및 `getCart()`·`save(Cart)` 인터페이스는 유지했습니다.

CartService의 담기·수량 변경·삭제에 `@Transactional`을 적용해 조회·변경·저장을 묶습니다. DatabaseCartRepository의 save에도 적용하여 저장소를 직접 호출해도 여러 SQL이 함께 성공하거나 취소됩니다. Service에서 호출하면 기본 전파 방식으로 같은 트랜잭션에 참여합니다.

행별로 저장해도 새 항목 INSERT와 다음 번호 UPDATE처럼 한 요청에 여러 쓰기가 필요합니다. 따라서 트랜잭션을 유지합니다. 테스트에서는 DELETE·INSERT가 실행된 뒤의 중복 오류, 수량 UPDATE 뒤 추가 INSERT 실패, 마지막 번호 UPDATE 실패를 발생시켜 앞선 변경도 모두 취소되는지 확인했습니다. 테스트 메서드 자체에는 `@Transactional`을 붙이지 않아 저장소의 실제 롤백을 검증합니다.

테스트 DB에만 표시용 컬럼을 추가하여, 수량 변경·추가·삭제 후 기존 행의 표시가 유지되는지도 확인했습니다. 전체 DELETE 후 INSERT하는 코드라면 이 표시가 없어지므로 최종 값만 같은 경우와 구분할 수 있습니다. 앱의 실제 테이블에는 이 테스트용 컬럼을 추가하지 않습니다.

외부 조회·가격 환산은 INSERT 전에 수행하므로 외부 요청 실패·환산 실패 시 기존 상품과 장바구니를 변경하지 않습니다. 위시리스트 등록·수정·삭제는 각 한 번의 쓰기입니다. 쿠폰·미리보기는 조회와 계산만 수행하며 save를 호출하지 않습니다.

## 테스트 DB 준비

앱 DB `wishlist`와 별도로 `wishlist_repository_test`를 사용합니다. `mysql>` 탭에서 최초 한 번 준비합니다.

```sql
CREATE DATABASE IF NOT EXISTS wishlist_repository_test
    DEFAULT CHARACTER SET utf8mb4;
CREATE USER IF NOT EXISTS 'wishlist_repository_test_user'@'%'
    IDENTIFIED BY 'wishlist_test_only';
GRANT ALL PRIVILEGES ON wishlist_repository_test.*
    TO 'wishlist_repository_test_user'@'%';
```

위 계정·암호는 로컬 테스트 전용이며 앱 root 계정·비밀번호와 별개입니다. 앱 DB 권한은 부여하지 않습니다. 테스트 접속 설정은 `src/test/resources/application-dbtest.properties`에만 있습니다. 테스트는 실제 MySQL 8.0.29에 연결하고, 앱의 테이블 생성 SQL을 재사용합니다.

각 DB 테스트 시작 시 실제 DB 이름과 인증 계정이 위 값인지 확인한 뒤 **테스트 DB의 테이블만** 재생성합니다. 앱 DB나 root에 연결되면 초기화 전에 실패하도록 했습니다. 이 DB는 반복 실행되는 테스트 전용 공간입니다. 테스트를 병렬로 실행하지 않습니다.

```powershell
# 기존 테스트와 DB 테스트 전체
.\gradlew.bat --console=plain test

# DB 통합 테스트 31개만
.\gradlew.bat --console=plain test --tests "*Database*Test"
```

이전 단위·HTTP·Spring 연결 테스트 131개는 기존 메모리 방식으로 유지합니다. 추가 DB 테스트는 외부 Client를 Mockito 대역으로 교체하여 실제 DummyJSON을 호출하지 않습니다.

## 단계별 완료 범위

| 범위 | 구현 내용 | 상태 |
|---|---|---|
| 기존 기능 | 위시리스트 등록·목록·단건 조회·예산 추천·이름 부분 검색·기존 화면 | 유지 |
| 1단계 | 외부 상품 목록·검색·상세 조회, 위시리스트 가져오기, 중복 방지, 원화 환산 | 완료 |
| 2단계 | 장바구니 담기·조회·수량 변경·삭제, 수량·종류·재고 제한, 복사한 값 보존 | 완료 |
| 2단계 | 쿠폰·배송비 포함 금액 미리보기, 오류 응답과 실패 후 데이터 보존 | 완료 |
| 보조 기능 | 수동 상품 수정·위시리스트 상품 삭제 | 완료 |
| DB 연동 | 위시리스트·장바구니 저장 및 복원, 중복 제약, 트랜잭션, 별도 MySQL DB 통합 테스트 | 완료 |
| 3단계 선택 | 외부 상품·장바구니·쿠폰 미리보기 웹 화면 | 완료 |
| 3단계 선택 | 배송비 포함 최저 금액 쿠폰 자동 추천 | 미구현 |

## 패키지 구조와 클래스 책임

```text
com.example.personal_project
├── PersonalProjectApplication
├── presentation
│   ├── ItemController / ItemDto
│   ├── ExternalProductController
│   ├── CartController
│   ├── AddCartItemRequest / ChangeQuantityRequest
│   ├── CartItemResponse / CartPreviewResponse
│   └── GlobalExceptionHandler
├── application
│   ├── ItemService
│   ├── CartService
│   └── ValidationService
├── domain
│   ├── Item / SourceType
│   ├── Cart / CartItem
│   ├── Coupon / CartPreview
│   ├── ItemRepository / CartRepository
│   └── EntityNotFoundException
└── infrastructure
    ├── MemoryItemRepository / MemoryCartRepository
    ├── DatabaseItemRepository / DatabaseCartRepository
    └── DummyJsonClient / DummyJsonProduct / DummyJsonProductList
```

| 클래스 | 책임 |
|---|---|
| Controller | HTTP 요청·응답 처리, 장바구니 요청 DTO의 `@Valid` 검증 |
| ItemService | 위시리스트 조회·등록·수정·삭제·추천, 외부 조회와 가져오기·가격 환산 조율 |
| CartService | 원본 상품과 보관된 Cart 조회, 담기·변경·삭제·미리보기 요청, 응답 DTO 변환 |
| ValidationService | 기존 이름·가격·예산 입력 검증과 이름 공백 제거 |
| Item | 로컬 id·이름·가격·출처·외부 id·재고 보관 |
| Cart | 장바구니 항목 관리, 중복 합산·20종 제한·행 id 생성·금액 계산 |
| CartItem | 처음 복사한 상품 정보 보관, 수량·재고 검사와 수량 변경 |
| Coupon | 쿠폰 종류, 할인 조건과 할인액 계산 |
| CartPreview | Domain의 금액 계산 결과 보관 |
| CartPreviewResponse | 계산 결과를 HTTP 응답으로 전달 |
| Repository 인터페이스 | 필요한 저장·조회 동작 정의 |
| MemoryItemRepository | 메모리 목록에 Item 저장·조회·수정·삭제, 로컬 id 생성 |
| MemoryCartRepository | 서버 메모리에서 하나의 Cart 보관 |
| DatabaseItemRepository | SQL로 위시리스트 저장·조회·수정·삭제, DB 생성 id 수신 및 행 복원 |
| DatabaseCartRepository | 장바구니 행·복사본·다음 번호의 SQL 저장과 복원 |
| DummyJsonClient | RestClient로 외부 HTTP 호출, 페이징·검색어 검사, 외부 오류 변환 |
| DummyJsonProduct / DummyJsonProductList | DummyJSON에서 받은 JSON 응답 보관 |
| GlobalExceptionHandler | 입력 오류·대상 없음·규칙 충돌을 400·404·409로 변환 |

Repository 인터페이스와 메모리 구현체를 분리하고, Service에 Repository와 외부 Client를 생성자로 주입합니다. 기존 방식에 맞춰 Service에서 응답 DTO 변환을 담당합니다.

## API 계약

기본 주소: `http://localhost:8081`. JSON 요청에는 `Content-Type: application/json`을 지정합니다. 삭제는 성공 시 `204`와 빈 본문을 반환하고, 아래 나머지 API는 성공 시 `200`을 반환합니다.

### 위시리스트

| 기능 | 메서드 | 경로 | 요청·응답 |
|---|---|---|---|
| 수동 등록 | POST | `/items` | 이름·가격 JSON → ItemDto |
| 전체 조회 | GET | `/items` | ItemDto 배열 |
| 단건 조회 | GET | `/items/{id}` | 로컬 id → ItemDto |
| 예산 추천 | GET | `/items/recommend?budget=5000` | ItemDto 배열, 최대 3개 |
| 이름 부분 검색 | GET | `/items/search?name=노트` | ItemDto 배열 |
| 수동 상품 수정 | PUT | `/items/{id}` | 이름·가격 JSON → ItemDto |
| 상품 삭제 | DELETE | `/items/{id}` | 위시리스트에서 삭제 |
| 외부 상품 가져오기 | POST | `/items/import/{externalId}` | 외부 id만 전달, 본문 없음 → ItemDto |

수동 등록·수정 요청 예시입니다.

```json
{"name": "  노트  ", "price": 12000}
```

응답 예시입니다. 요청으로 id를 보내도 등록 시에는 서버가 새 id를 부여하고, 수정 시에는 경로의 id를 사용합니다.

```json
{"id": 1, "name": "노트", "price": 12000}
```

응답으로 받은 실제 id를 이후 조회·수정·삭제에 사용합니다. 이름은 null·빈 문자열·공백뿐인 값을 거부하고 앞뒤 공백을 제거합니다. 가격은 0 이상의 int이며 무료 상품을 허용합니다. 예산도 0 이상이어야 합니다.

추천은 **각 상품의 가격이 예산 이하인지** 검사하고, 가격 오름차순으로 정렬한 뒤 최대 3개를 반환합니다. 같은 가격은 등록 순서를 유지하며 원본 목록을 변경하지 않습니다. 추천 상품들의 합계를 예산에 맞추는 기능은 아닙니다. 이름 부분 검색은 누락·빈 검색어·공백 검색어를 거부합니다. 추천·이름 검색 결과가 없으면 빈 배열을 반환합니다.

### 외부 상품 조회

| 기능 | 내 서버 경로 | DummyJSON 요청 |
|---|---|---|
| 목록 | `/external-products?limit=10&skip=0` | `/products?limit=10&skip=0` |
| 검색 | `/external-products/search?q=phone&limit=10&skip=0` | `/products/search?q=phone&limit=10&skip=0` |
| 상세 | `/external-products/1` | `/products/1` |

모두 GET 요청이며, 브라우저 또는 Postman → 내 Spring 서버 → `https://dummyjson.com` 순서로 호출합니다. 연결 제한은 3초, 응답 제한은 5초입니다.

- `q`: 필수, 앞뒤 공백 제거 후 빈 값 거부
- `limit`: 기본 10, 1~20
- `skip`: 기본 0, 0 이상
- 외부 상세 id: 양수
- 상세 응답: `id`, `title`, `price`, `category`, `thumbnail`, `stock`
- 목록·검색 응답: `{"products": [...]}`. 결과 없음은 200과 `{"products": []}`

외부 조회 응답의 `price`는 원래 가격이며, 위시리스트로 가져올 때만 수업용 원화로 환산합니다. `discountPercentage`, `minimumOrderQuantity`는 사용하지 않습니다.

### 외부 상품 가져오기 규칙

`POST /items/import/1`처럼 외부 id만 받습니다. 서버가 상세 API를 조회하여 이름·가격·재고를 가져오므로 요청으로 이름·가격을 지정하지 않습니다.

- 로컬 `id`는 서버에서 새로 생성하며, `sourceType=DUMMY_JSON`과 `externalId`를 별도로 보관합니다.
- 수동 등록 상품은 `sourceType=MANUAL`, 외부 id·재고는 null입니다.
- 같은 출처·외부 id가 이미 있으면 409로 거부합니다.
- 재고 0인 품절 상품도 위시리스트에는 저장할 수 있습니다.
- 저장된 외부 상품의 이름·가격은 PUT 요청으로 수정할 수 없고, 외부 API의 변경 내용을 자동으로 갱신하지 않습니다.
- 가져오기 응답은 기존 ItemDto의 `id`, `name`, `price`입니다. 출처·외부 id·재고는 서버의 Item에 보관합니다.

환산은 `BigDecimal`로 1,000을 곱한 뒤 `HALF_UP`으로 원 단위 반올림합니다. 예를 들어 9.99 → 9,990원, 9.9905 → 9,991원입니다. 실제 통화나 실시간 환율이 아닌 수업용 고정 규칙입니다.

### 장바구니

| 기능 | 메서드 | 경로 | 요청·응답 |
|---|---|---|---|
| 조회 | GET | `/cart` | CartItemResponse 배열 |
| 담기 | POST | `/cart/items` | `{"itemId": 1, "quantity": 2}` → CartItemResponse |
| 수량 변경 | PATCH | `/cart/items/{id}` | `{"quantity": 3}` → CartItemResponse |
| 행 삭제 | DELETE | `/cart/items/{id}` | 해당 행 삭제 |

`itemId`는 위시리스트의 로컬 상품 id입니다. PATCH·DELETE 경로의 `id`는 장바구니 행 id이므로 담기 응답 또는 목록에서 확인합니다. 수량 변경은 기존 수량에 더하는 것이 아니라 요청한 수량으로 교체합니다.

장바구니 행 응답 예시입니다. 아래 null 재고는 수동 상품에 재고 제한이 없음을 뜻합니다.

```json
{
  "id": 1,
  "itemId": 10,
  "name": "노트",
  "unitPrice": 12000,
  "sourceType": "MANUAL",
  "stock": null,
  "quantity": 2
}
```

- 추가·변경 요청 수량은 1~5개이며, 같은 상품을 추가하면 기존 행에 합산합니다. 합산 후에도 최대 5개입니다.
- 서로 다른 상품은 최대 20종입니다. 이미 20종이어도 기존 상품에 허용 범위의 수량을 추가할 수 있습니다.
- 외부 상품은 복사한 재고 이하만 허용하며, 품절·재고 정보 누락은 거부합니다. 수동 상품은 재고 제한이 없습니다.
- 처음 담을 때 이름·단가·출처·재고를 복사합니다. 원본 수정·삭제 후에도 장바구니 값은 유지됩니다.
- 중복 추가·수량 변경은 처음 복사한 단가·재고를 사용합니다. 장바구니에서 삭제 후 다시 담으면 현재 위시리스트 값을 새로 복사합니다.
- 새로 담거나 중복 추가할 때는 로컬 원본 상품이 있어야 합니다. 원본 삭제 후에도 기존 장바구니 행의 조회·수량 변경·삭제·미리보기는 가능합니다.

### 금액 미리보기

```http
GET /cart/preview
GET /cart/preview?couponCode=PERCENT10
GET /cart/preview?couponCode=FIXED15000
```

`couponCode`를 생략하면 `NONE`이며, 한 번에 하나만 적용합니다. 쿠폰 코드는 아래 대문자 값을 사용합니다.

| 쿠폰 | 할인 규칙 |
|---|---|
| NONE | 할인 없음 |
| PERCENT10 | 상품 합계의 10%, 원 미만 버림, 최대 20,000원 |
| FIXED15000 | 상품 합계 100,000원 이상일 때 15,000원 |

단가·수량의 곱과 상품 합계·할인·배송비·최종 금액은 long으로 계산합니다. 할인 후 금액이 100,000원 이상이면 무료배송이고, 미만이면 5,000원입니다. 빈 장바구니는 모든 금액이 0이며 NONE만 허용합니다. 0원 상품이 담긴 장바구니는 빈 장바구니가 아니므로 배송비 규칙을 적용합니다.

상품 합계 110,000원에 PERCENT10을 적용한 응답입니다.

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

미리보기는 계산 결과를 반환할 뿐, 결과를 저장하거나 장바구니의 항목·수량·복사한 정보를 변경하지 않습니다.

### 오류 응답과 데이터 보존

| 상태 | 상황 |
|---|---|
| 400 | 잘못된 이름·가격·예산·검색어·페이징·요청 수량, 알 수 없거나 사용할 수 없는 쿠폰 |
| 404 | 없는 로컬 상품·외부 상품·장바구니 행 |
| 409 | 외부 상품 중복 가져오기·이름/가격 수정 시도, 20종 제한·합산 수량·재고 충돌 |
| 502 | 외부 연결 실패·시간 초과·외부 서버 오류·빈 외부 응답 본문 |

요청 수량이 6개이면 입력 검증 오류인 400이고, 기존 3개에 3개를 추가하여 합산 수량이 6개가 되면 규칙 충돌인 409입니다.

검증과 외부 조회를 마친 뒤 저장하고, 수량도 검사 후 변경하여 실패한 가져오기·추가·변경·미리보기가 기존 데이터를 바꾸지 않도록 했습니다. 공통 핸들러의 응답 본문은 예외 메시지 문자열이며, DTO 검증과 `ResponseStatusException`은 Spring의 오류 응답을 사용합니다. 오류 본문 형식을 하나로 통일하지는 않았습니다.

## 자동 테스트와 실제 검증 결과

단위 테스트는 Domain·Service의 규칙을 확인하고, MockMvc 테스트는 요청 DTO 검증과 HTTP 상태·응답·저장소 보존을 함께 확인합니다. `@SpringBootTest`로 Spring이 연결한 Service와 실제 메모리 저장소도 확인합니다.

외부 Client 테스트는 `MockRestServiceServer`로 가짜 응답·연결 실패·시간 초과 예외를 준비합니다. Service·HTTP 연계 테스트의 외부 호출은 Mockito Client 대역을 사용하며 실제 DummyJSON에 접속하지 않습니다. 실제 네트워크에서 3초·5초가 경과하는지 측정한 테스트는 아닙니다.

DB 연동 후 현재 소스와 설정을 그대로 복사한 검증 폴더에서 `.\gradlew.bat --offline test --console=plain`을 실행하여 `BUILD SUCCESSFUL`을 확인했습니다. 원본 폴더에서 Gradle 프로세스를 시작하는 실행 환경의 권한 제한 때문에 복사본을 사용했으며, 아래 수치는 그 실행의 테스트 결과 XML 기준입니다.

| 테스트 클래스 | 실행 | 실패 | 오류 | 건너뜀 |
|---|---:|---:|---:|---:|
| PersonalProjectApplicationTests | 1 | 0 | 0 | 0 |
| CartServiceIntegrationTest | 2 | 0 | 0 | 0 |
| ItemServiceTest | 23 | 0 | 0 | 0 |
| ItemServiceUnitTest | 11 | 0 | 0 | 0 |
| CartItemTest | 12 | 0 | 0 | 0 |
| CartPreviewTest | 8 | 0 | 0 | 0 |
| CartTest | 22 | 0 | 0 | 0 |
| DatabaseApiIntegrationTest | 9 | 0 | 0 | 0 |
| DatabaseCartRepositoryTest | 12 | 0 | 0 | 0 |
| DatabaseItemRepositoryTest | 10 | 0 | 0 | 0 |
| DummyJsonClientTest | 20 | 0 | 0 | 0 |
| CartControllerTest | 26 | 0 | 0 | 0 |
| CartPreviewControllerTest | 6 | 0 | 0 | 0 |
| **합계** | **162** | **0** | **0** | **0** |

테스트 실행 후 보고서는 실행한 프로젝트의 `build/reports/tests/test/index.html`, XML은 `build/test-results/test/`에서 확인할 수 있습니다. 위 기록은 test 작업 결과이며, 별도 build 작업 완료를 의미하지 않습니다.

### QA 체크리스트와 결과

| 검증 항목 | 실제 결과 |
|---|---|
| 기존 등록·실제 id 조회·검색·예산 추천·원본 목록 보존 | 통과 |
| 외부 상세·목록·검색의 정상/빈 결과·입력 오류·404·502 | 통과 |
| 연결 실패·시간 초과 예외를 502로 변환 | 통과 |
| 9.99 → 9,990원, 9.9905 → HALF_UP 9,991원 | 통과 |
| 로컬 id와 외부 id 구분, 출처·재고 보존 | 통과 |
| 중복 가져오기 409, 품절 가져오기 허용 및 장바구니 담기 거부 | 통과 |
| 요청 수량 1~5·동일 상품 합산·20종·재고 경계 | 통과 |
| 잘못된 추가·수량 변경·없는 행 삭제 후 기존 값 보존 | 통과 |
| 원본 수정·삭제 후 장바구니 정보 유지, 원본 삭제 후 수량 변경·행 삭제 | 통과 |
| 장바구니 삭제 후 재추가 시 현재 이름·단가·재고 복사 | 통과 |
| 원본이 없는 상품의 추가 거부, 다른 위시리스트·장바구니 보존 | 통과 |
| 쿠폰 최소 금액·원 미만 버림·할인 상한·배송비 100,000원 경계 | 통과 |
| 빈 장바구니 NONE만 허용, 모든 금액 0 | 통과 |
| 무료 상품이 담긴 장바구니의 배송비 적용 | 통과 |
| int 범위를 넘는 합계를 long으로 계산 | 통과 |
| 정상·실패 미리보기 전후 장바구니 보존 | 통과 |
| Spring이 주입한 Service의 장바구니·미리보기 실행 | 통과 |

상품 합계 110,000원의 쿠폰별 결과도 자동 테스트에서 확인했습니다.

| 쿠폰 | 할인 | 할인 후 금액 | 배송비 | 최종 금액 |
|---|---:|---:|---:|---:|
| NONE | 0 | 110,000 | 0 | 110,000 |
| PERCENT10 | 11,000 | 99,000 | 5,000 | 104,000 |
| FIXED15000 | 15,000 | 95,000 | 5,000 | 100,000 |

## 웹 화면 사용과 검증

- 위시리스트: 직접 등록·이름 검색·예산 추천·수동 수정·삭제·수량 지정 후 장바구니 담기
- 외부 상품: 목록·키워드 검색·페이지 이동·썸네일·위시리스트 가져오기. 품절 상품도 가져오기 가능하며 실제 저장 가격은 서버가 계산합니다.
- 장바구니: 원본 위시 id와 장바구니 행 id 표시·수량 변경·삭제·처음 복사한 단가/재고 표시
- 금액: 쿠폰 선택에 따라 상품 합계·할인·할인 후 금액·배송비·최종 예상 금액 표시
- 실패 처리: 서버의 문자열/JSON 오류, 삭제 204의 빈 응답, 사용할 수 없는 쿠폰, 중복 가져오기·외부 상품 수정 409를 안내합니다. 실패한 금액 계산에는 이전 금액을 남기지 않습니다.
- 저장 안내: DB 모드(prod)는 재시작 후 유지, 메모리 모드(default·test)는 초기화된다고 구분해 표시합니다. 브라우저 자체에 샘플 데이터를 저장하지 않습니다.

검증용 복사본 서버(8091)를 테스트 전용 DB에 연결하여 Chromium 브라우저에서 실제 API 연동을 확인했습니다. 앱 DB wishlist에는 테스트 데이터를 추가하거나 삭제하지 않았습니다.

| 브라우저 확인 | 결과 |
|---|---|
| 수동/무료 등록·검색·추천·수정 | 통과 |
| 110,000원에서 NONE 110,000원·PERCENT10 104,000원·FIXED15000 100,000원 | 통과 |
| 수량 변경·할인 상한·원본 수정/삭제 후 장바구니 유지 | 통과 |
| 위시리스트/장바구니 삭제의 204 빈 본문 처리 | 통과 |
| 빈 장바구니에서 쿠폰 오류와 오래된 금액 제거 | 통과 |
| 실제 DummyJSON 목록·검색·가져오기·중복 409·외부 상품 수정 409 | 통과 |
| 실제 외부 목록 페이지 이동·검색 결과 없음 | 통과 |
| 외부 502 응답 안내·재시도 버튼 복구 | 준비한 오류 응답으로 통과 |
| 페이지 새로고침 후 서버 저장값 재조회 | 통과 |
| 데스크톱·390px 모바일 화면, JavaScript 실행 오류 없음 | 통과 |

화면을 수정한 뒤에는 Spring 서버를 재시작하고 브라우저에서 강력 새로고침(Ctrl+F5)합니다. DB 모드에서 이 과정은 저장된 상품·장바구니를 초기화하지 않습니다.

## DB 연동 QA 기록

### 사용자와 수동으로 확인한 결과

| 시나리오 | 실제 결과 |
|---|---|
| 수동 상품 등록 후 MySQL 조회 | 노트 12,000원·MANUAL·외부 id/재고 NULL 저장 |
| 앱 재시작 후 기존 상품 조회 | 같은 DB에서 기존 상품 복원 |
| 수동 수정·잘못된 수정 | 볼펜 2,000원 수정 성공, 음수 가격은 400과 기존 값 유지 |
| 외부 가져오기·중복·수정 거부 | 9.99 → 9,990원, DUMMY_JSON·external_id=1·stock=99 저장, 중복·외부 수정 409 |
| 위시리스트 삭제 | 204, 삭제 후 조회·재삭제 404 |
| 장바구니 담기·재시작 | 원본 id=2에서 행 id=1·수량 2 복사, 재시작 후 복원 |
| 수량 변경·합산 실패 | 수량 3 저장, 3개 추가하여 6개가 되는 요청은 409·수량과 다음 번호 유지 |
| 삭제 후 다시 담기 | 삭제한 번호 재사용 없이 행 id=2·next_sequence=3으로 진행 |
| 원본 삭제 후 장바구니 사용 | 원본 items는 비고 cart_items의 행 id=3·단가 9,990원·재고 99 유지, 수량 2로 변경 |
| 미리보기 전후 DB 상태 | 행 id=3·단가 9,990원·재고 99·수량 2·next_sequence=4 유지 |
| MySQL 터미널 한글 표시 | HEX(name)으로 UTF-8 정상 저장 확인, SET NAMES utf8mb4 후 표시 정상 |

### 추가 자동 검증 결과

별도 MySQL DB에서 DB Repository 22개와 DB API·Service 연계 9개, 합계 31개 테스트를 통과했습니다. 수동으로 아직 확인하지 않은 원본 수정 후 복사본 유지·현재 값 재담기, 이름 중복·가격 0·동률 순서, SQL 중간 실패 롤백은 자동 테스트로 검증했습니다.

- DB 생성 id, NULL·출처·외부 id·재고의 객체 복원, 새 Repository 객체를 통한 재조회
- SQL 목록·검색의 등록 순서, 대소문자와 특수문자의 문자 그대로 검색
- Service 사전 검사 없이 DB UNIQUE가 중복을 차단하고 기존 값 보존
- 원본 수정·삭제 후 장바구니 유지, 재담기 시 현재 이름·단가·재고 복사
- 모든 장바구니 행 삭제 후에도 다음 번호 보존
- 수량 변경·추가·삭제는 변경한 행만 반영하며 기존 행 재생성 없음
- 두 번째 INSERT 실패·수량 UPDATE 후 INSERT 실패·마지막 번호 UPDATE 실패 시 전체 롤백
- 입력 400·대상 없음 404·규칙 충돌 409·외부 실패 502와 DB 보존
- 쿠폰별 최종 금액·빈 장바구니·정상/실패 미리보기 후 저장 상태 보존

검증은 현재 소스와 설정을 그대로 복사한 폴더에서 실제 로컬 MySQL 테스트 DB를 연결하여 수행했습니다. Gradle 결과는 **162개 실행·실패 0·오류 0·건너뜀 0, BUILD SUCCESSFUL**입니다. 실행 환경의 원본 폴더 접근 제한 때문에 복사본을 사용했고, 앱 DB를 초기화하지 않았습니다.

