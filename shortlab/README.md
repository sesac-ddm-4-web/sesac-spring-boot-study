# ShortLab: 단축 URL 서비스

Spring Boot로 만든 단축 URL 서버입니다. 긴 URL을 짧은 링크로 바꾸고, 접속하면 원본 URL로 이동(302)시키며 접속 횟수를 셉니다. Map 저장을 먼저 구현한 뒤 **Docker MySQL 연동**으로 확장하고, 직접 기획한 **별칭, QR 코드, 클릭 로그** 기능을 더했습니다. 실행 방법, 설계 결정, 테스트·QA 결과를 이 README에 기록했습니다.

- Java 21 / Spring Boot 4.1.2 / Gradle(Kotlin DSL, `build.gradle.kts`)
- DB: MySQL 8.4 (Docker), Spring Data JPA
- QR 코드: [ZXing](https://github.com/zxing/zxing) (서버에서 SVG 생성)
- 화면: 순수 HTML/CSS/JS (`src/main/resources/static/`, 외부 라이브러리·CDN 없음)<img width="738"

## 서비스 안내

단축 URL 서비스의 핵심(생성, 리다이렉트, 접속 횟수)을 구현한 뒤, 같은 프로젝트를 확장했습니다. 이 저장소에는 두 가지가 함께 있습니다.

| 구분 | 내용 |
|---|---|
| 책 실습 | 단축 URL 생성, 302 리다이렉트, 접속 횟수 (Map 컬렉션 저장) |
| 개인 과제 | DB 연동, 클릭 로그, 별칭, QR 코드, 화면 |

## 기획 요구사항과 구현 목표

### 기획 요구사항

- 사용자가 원본 URL을 보내면 짧은 URL이 만들어진다.
- 단축 URL로 접속하면 원본 URL로 이동하고, 접속 횟수가 1 증가한다.
- 단축 URL의 정보(원본 URL, 접속 횟수)를 조회할 수 있다.
- 서버를 껐다 켜도 링크와 접속 횟수가 남아 있어야 한다. (2단계)

### 구현 목표 (직접 추가한 기능)

| 기능 | 목표 |
|---|---|
| 클릭 로그 | 접속할 때마다 시각을 기록하고, 링크별 최근 접속 기록을 보여 준다. 로그 저장이 실패해도 리다이렉트는 막지 않는다. |
| 별칭 | 사용자가 원하는 이름(예: `my-menu`)으로 링크를 만들 수 있다. 비우면 랜덤 키를 쓴다. 중복·예약어는 거절한다. |
| QR 코드 | 단축 링크가 담긴 QR을 만들고 SVG·PNG로 저장할 수 있다. 스캔해도 서버를 거치므로 접속 기록이 남는다. |

## 단계별 구현 범위

| 단계 | 구현한 것 |
|---|---|
| 1단계 | **Map 컬렉션에 저장**, 단축 URL 생성(8자리 base56 키, 충돌 시 5회 재시도), 302 리다이렉트, 접속 횟수(`AtomicLong`), 조회, 예외 변환(404, 500). 클릭 로그·별칭·QR 없음 |
| 2단계 | **DB 연동.** Docker MySQL 8.4 + JPA. 서비스 코드는 두고 저장소 구현체만 교체(`MysqlShortenUrlRepository`). 접속 횟수를 DB에서 원자적으로 증가. 화면(HTML/CSS/JS) 추가 |
| 3단계 | **클릭 로그, 별칭, QR 코드** 추가 |

### 1단계. Map 컬렉션 저장

- `MapShortenUrlRepository`가 `Map`에 단축 키와 `ShortenUrl`을 저장합니다. 서버를 끄면 데이터가 사라집니다.
- 단축 키는 헷갈리는 문자(`0, 1, I, O, l, o`)를 뺀 base56 8자리입니다. 이미 있는 키면 최대 5번 다시 만들고, 그래도 실패하면 `LackOfShortenUrlKeyException`(500)입니다.
- 단축 URL로 접속하면 `302`와 `Location` 헤더로 원본 URL에 보내고 접속 횟수를 올립니다. 301은 브라우저가 캐시해 서버를 거치지 않아 횟수를 셀 수 없어서 쓰지 않았습니다.
- 접속 횟수는 여러 요청이 동시에 와도 안전하도록 `AtomicLong`을 씁니다.

### 2단계. DB 연동 (Docker MySQL)

메모리에만 저장하던 데이터를 Docker로 띄운 MySQL에 저장하도록 바꿨습니다.

- `ShortenUrlRepository`가 domain의 인터페이스라서, 서비스 코드는 바꾸지 않고 구현체만 `MysqlShortenUrlRepository`로 교체했습니다. `MapShortenUrlRepository`는 비교용으로 남기되 `@Repository`를 붙이지 않았습니다.
- 도메인 객체(`ShortenUrl`)와 DB용 객체(`ShortenUrlEntity`)를 분리했고, 변환은 `MysqlShortenUrlRepository`가 합니다.
- 접속 횟수 증가는 `update ... set redirect_count = redirect_count + 1` 쿼리 한 문장으로 처리해, 동시 접속에서도 횟수가 사라지지 않습니다. (`@Modifying @Query`)
- `ddl-auto=validate`로 테이블은 직접 SQL로 만들고, 엔티티와 어긋나면 앱 시작 때 실패하게 했습니다.
- 키 컬럼은 `utf8mb4_bin`으로 대소문자를 구분합니다.

### 3단계. 클릭 로그 · 별칭 · QR 코드

**클릭 로그**
- 단축 URL로 접속할 때마다 `click_log`에 (단축 키, 접속 시각)을 한 줄씩 저장합니다. 시각은 `Asia/Seoul` 기준입니다.
- `ClickLogRepository`(domain 인터페이스)와 `MysqlClickLogRepository`(infrastructure)로 분리했습니다.
- 로그 저장은 `try/catch`로 감싸, 로그 오류가 리다이렉트를 막지 않습니다.
- 최근 접속 시각 20건을 최신순으로 조회합니다. (`findTop20ByShortenUrlKeyOrderByClickedAtDesc`)
- 실제 화면
  <img width="697" height="472" alt="스크린샷 2026-11-15 오전 10 35 21" src="https://github.com/user-attachments/assets/5b3f1649-72fb-4cb0-a8df-d54deca00f63" />


**별칭**
- 생성 요청에 `alias`(선택)를 보냅니다. 없으면 랜덤 키, 있으면 그 값이 단축 키가 됩니다.
- 영문·숫자·하이픈, 3~20자, 영문이나 숫자로 시작해야 합니다. (`@Pattern`)
- 이미 있거나 예약어(`shortenUrl`, `assets`, `error`, `index`, `favicon`)면 `DuplicateShortenUrlKeyException`(409)입니다. 동시에 만들어 DB 제약에 걸린 경우도 409로 바꿉니다.
- 대소문자를 구분합니다. 컨트롤러 경로의 정규식은 `/{shortenUrlKey:[A-Za-z0-9-]+}`입니다. 점(`.`)이 없어서 `index.html` 같은 정적 파일은 키로 잡히지 않습니다.
- 실제 화면
  <img width="738" height="347" alt="스크린샷 2026-11-15 오전 10 34 23" src="https://github.com/user-attachments/assets/669347ab-214c-4def-a2b4-5e12644cdad8" />


**QR 코드**
- `QrCodeService`가 단축 링크를 ZXing으로 QR로 만들어 **SVG**로 응답합니다. 확대해도 깨지지 않고, PNG는 화면에서 변환해 저장합니다.
- QR에는 원본 URL이 아니라 **단축 링크**가 담깁니다. 그래서 QR을 스캔해도 서버를 거쳐 접속 기록이 남습니다.
- 단축 링크의 주소는 요청을 보낸 호스트로 만들어집니다. `localhost`로 만든 QR은 휴대폰에서 열리지 않으므로, 휴대폰으로 확인하려면 Mac의 IP(예: `192.168.0.10:8080`)로 접속해 링크를 만들어야 합니다.
- 실제 화면
  <img width="650" height="469" alt="스크린샷 2026-11-15 오전 10 33 41" src="https://github.com/user-attachments/assets/e7db980b-5b0d-4189-82ab-d19648367da0" />


### 화면

순수 HTML/CSS/JS로 만든 한 페이지입니다. 링크 만들기(별칭 입력), 결과 카드(단축 URL, QR, 접속 횟수), 링크 통계 조회(접속 횟수, 최근 접속 기록), 최근 만든 링크 목록이 있습니다. 접속 횟수와 기록은 5초마다 자동으로 갱신됩니다.

<!-- TODO: 화면 스크린샷을 docs/images/에 넣고 아래 줄의 주석을 풀어 주세요.
![메인 화면](docs/images/main.png)
![QR 코드](docs/images/qr.png)
![접속 기록](docs/images/clicklog.png)
-->

## 실행 방법

### 1. MySQL 컨테이너

```bash
docker run --name shortlab-mysql \
  -e MYSQL_ROOT_PASSWORD=<비밀번호> \
  -p 3307:3306 \
  -v shortlab-mysql-data:/var/lib/mysql \
  -d mysql:8.4
```

호스트 포트는 3307입니다. (3306이 이미 사용 중이어서) Docker Desktop이 켜져 있어야 합니다.

### 2. 스키마

DDL은 `<schema.sql 경로>`를 참고하세요. 앱은 `ddl-auto=validate`라서 **앱을 켜기 전에** 테이블을 만들어야 합니다.

### 3. 실행

```bash
./gradlew bootRun
```

브라우저에서 `http://localhost:8080`을 엽니다.

## API 명세

| 메서드 | 경로 | 설명 | 성공 | 오류 |
|---|---|---|---|---|
| POST | `/shortenUrl` | 단축 URL 생성. 본문 `{"originalUrl":"https://...","alias":"my-menu"}` (`alias`는 선택) | 200 | 400(URL·별칭 형식), 409(별칭 중복·예약어), 500(키 생성 실패) |
| GET | `/{key}` | 원본 URL로 302 이동, 접속 횟수 증가, 클릭 로그 기록 | 302 | 404 |
| GET | `/shortenUrl/{key}` | 원본 URL과 접속 횟수 조회 | 200 | 404 |
| GET | `/shortenUrl/{key}/clicks` | 최근 접속 시각 20건 (최신순) | 200 | 404 |
| GET | `/shortenUrl/{key}/qr` | 단축 링크의 QR 코드 (SVG) | 200 | 404 |

응답 필드: `originalUrl, shortenUrlKey, redirectCount` (조회), 생성 응답은 생성된 단축 키 정보를 포함합니다.

> 성공 상태 코드는 실제 컨트롤러 응답에 맞춰 확인해 주세요.

### 오류 코드 정리

| 코드 | 의미 | 예 |
|---|---|---|
| 400 | 요청 값이 잘못됨 | URL이 `http(s)`가 아님, 별칭 형식 위반(2자, 한글 등) |
| 404 | 대상이 없음 | 존재하지 않는 단축 키 |
| 409 | 현재 상태와 충돌 | 이미 사용 중인 별칭, 예약어 |
| 500 | 서버가 처리할 수 없음 | 랜덤 키를 5번 만들어도 모두 충돌 |

## 구조와 클래스 책임

의존 방향: `presentation → application → domain ← infrastructure` (domain은 다른 계층을 import하지 않음). 기본 패키지는 `com.example.shortlab`입니다.

| 계층 | 주요 클래스 | 책임 |
|---|---|---|
| presentation | `ShortenUrlRestController`, `GlobalExceptionHandler` | HTTP 입·출력, 302 응답, 예외를 상태코드로 변환 |
| presentation.dto | `ShortenUrlCreateRequestDto`, `ShortenUrlCreateResponseDto`, `ShortenUrlInformationDto` | 요청·응답 형태, 입력 검증(`@NotNull`, `@URL`, `@Pattern`) |
| application | `SimpleShortenUrlService`, `QrCodeService` | 흐름 조율(키·별칭 확인, 저장, 카운트, 로그), QR SVG 생성 |
| domain | `ShortenUrl` | 단축 URL 규칙(키 생성, 접속 횟수 증가) |
| domain (인터페이스) | `ShortenUrlRepository`, `ClickLogRepository` | 바깥 계층이 구현할 저장·조회 약속 |
| domain.exception | `NotFoundShortenUrlException`, `LackOfShortenUrlKeyException`, `DuplicateShortenUrlKeyException`, 'InvalidOriginalUrlException' | 의미 있는 실패 종류 (HTTP를 모름) |
| infrastructure | `MysqlShortenUrlRepository`, `MapShortenUrlRepository`, `MysqlClickLogRepository`, `ShortenUrlEntity`, `ClickLogEntity`, `ShortenUrlJpaRepository`, `ClickLogJpaRepository` | DB 저장소 구현, 엔티티, Spring Data JPA 인터페이스 |

핵심 설계 결정:
- 서비스는 `ShortenUrlRepository`·`ClickLogRepository` 인터페이스에만 의존합니다. 저장소를 Map에서 MySQL로 바꿀 때 서비스는 그대로였습니다.
- DTO, 도메인, 엔티티를 나눴습니다. 요청 형식이 바뀌어도 도메인과 DB 구조에 영향이 없습니다.
- 접속 횟수는 DB의 증가 쿼리로 처리합니다. "읽고, 더하고, 저장" 방식은 동시 접속에서 횟수가 사라집니다.
- 클릭 로그 저장 실패가 리다이렉트를 막지 않도록 격리했습니다.
- 정적 파일과 단축 키가 충돌하지 않도록 경로에 정규식을 걸고, 일부 이름은 예약어로 막았습니다.

## DB 스키마

| 테이블 | 설명 | 제약 |
|---|---|---|
| `shorten_url` | 단축 URL | PK, `shorten_url_key` UNIQUE(`utf8mb4_bin`, 대소문자 구분), `original_url`·`redirect_count` NOT NULL, 접속 횟수 기본 0 |
| `click_log` | 접속 기록 | PK(AUTO_INCREMENT), `shorten_url_key`, `clicked_at` NOT NULL, (`shorten_url_key`, `clicked_at`) 인덱스 |

`click_log`는 `shorten_url`과 외래 키로 연결하지 않고 단축 키 문자열만 저장합니다.

전체 DDL은 `<schema.sql 경로>`를 참고하세요.

## 테스트

| 테스트 | 검증 내용 |
|---|---|
| `SimpleShortenUrlServiceTest` | 키가 계속 충돌하면 `LackOfShortenUrlKeyException`(자원 부족), 생성·조회 (Mockito `@Mock`) |
| `ShortenUrlRestControllerTest` | 단축 URL 접속 시 302와 `Location` 헤더 (`@WebMvcTest`, `@MockitoBean`) |
| `QrCodeServiceTest` | QR이 올바른 SVG로 만들어지는지, 같은 입력이면 같은 결과인지 |

## QA 체크리스트

직접 실행한 결과를 기록합니다. (`결과` 칸은 실행 후 기입)

| # | 항목 | 방법 | 기대 | 결과 |
|---|---|---|---|---|
| 1 | 단축 URL 생성 | `POST /shortenUrl {"originalUrl":"https://example.com"}` | 200, 8자리 키 | 200, 8자리 키 |
| 2 | 잘못된 URL | `originalUrl`을 `abc`로 | 400 | 400 |
| 3 | 리다이렉트 | 브라우저에서 단축 URL 접속 | 원본 URL로 이동(302) | 302 |
| 4 | 접속 횟수 | 3번 접속 후 `GET /shortenUrl/{key}` | `redirectCount` 3 | `redirectCount` 3 |
| 5 | 없는 키 | `GET /없는키` | 404 | 404 |
| 6 | 서버 재시작 | 서버를 껐다 켠 뒤 같은 링크 접속 | 링크와 횟수 유지 | 링크와 횟수 유지 |
| 7 | 별칭 지정 | `alias`에 `my-menu` | 단축 키가 `my-menu` | 단축 키가 `my-menu` |
| 8 | 별칭 중복 | 같은 별칭으로 다시 생성 | 409 | 409 |
| 9 | 별칭 예약어 | `alias`에 `assets` | 409 | 409 |
| 10 | 별칭 형식 | `ab`, 한글, `-abc` | 400 | 400 |
| 11 | 클릭 로그 | 접속 후 `GET /shortenUrl/{key}/clicks` | 접속 시각이 최신순으로 | 접속 시각이 최신순으로 |
| 12 | QR 이미지 | `GET /shortenUrl/{key}/qr` | SVG 응답 | SVG 응답|
| 13 | QR 스캔 | 휴대폰으로 QR 스캔 (Mac IP로 접속해 생성) | 원본 URL로 이동 | 원본 URL로 이동|
| 14 | 화면 동작 | 링크 생성 → QR 저장 → 통계 조회 | 정상 | 정상 |
| 15 | 전체 테스트 | `./gradlew test` | 전부 통과 | 15 |

## 남은 이슈와 정책

- **링크 만료일은 아직 없습니다.** 생성할 때 기간을 정하고, 만료된 링크는 원본으로 보내지 않는 기능을 다음 과제로 진행할 예정입니다.
- **사용자 구분(로그인)이 없습니다.** 모든 링크가 공개이고, "최근 만든 링크" 목록은 브라우저의 localStorage에 저장되어 새 브라우저에서는 비어 있습니다. 통계는 키만 알면 누구나 조회할 수 있습니다.
- **클릭 로그는 리다이렉트 안에서 동기로 저장합니다.** 트래픽이 많아지면 응답이 느려질 수 있어 비동기 저장이 필요합니다. 저장 실패는 일부러 삼키므로 로그가 빠질 수 있습니다.
- **QR은 요청한 호스트 주소로 만들어집니다.** `localhost`로 만든 QR은 휴대폰에서 열리지 않습니다.
