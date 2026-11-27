# 프린트 링크 (Print Link)

휴대폰에 있는 PDF나 웹 링크를 공용 PC에서 숫자 8자리 코드로 열 수 있는 단축 URL 서비스입니다.

**[서비스 바로가기](https://ezprint.up.railway.app/)**

## 1. 기획 배경

프린트 카페에서 휴대폰에 저장된 PDF를 출력하려면 공용 PC에서 개인 메신저나 이메일에 로그인하고 파일을 찾아야 하는 경우가 있습니다. 자료가 있는 기기와 출력할 기기가 서로 다르기 때문에 링크 복사만으로는 해결하기 어렵습니다.

이 문제에 단축 URL의 원리를 적용했습니다. 사용자가 휴대폰에서 자료를 등록하면 서버가 숫자 8자리 코드를 발급하고, 공용 PC에서는 코드로 자료를 찾아 열 수 있습니다.

프린트 카페 운영자가 공용 PC에 서비스 즐겨찾기나 바로가기를 미리 제공하는 사용 환경을 가정했습니다. 이용자가 긴 서비스 주소를 매번 직접 입력하는 불편도 줄일 수 있습니다.

## 2. 사용 방법

1. 휴대폰에서 프린트 링크에 접속합니다.
2. PDF를 선택하거나 웹 링크를 입력하고 **8자리 코드 발급**을 누릅니다.
3. 공용 PC에서 같은 서비스에 접속한 후 코드를 입력하고 **자료 열기**를 누릅니다.
4. 새 탭에서 자료를 확인하고 브라우저의 인쇄 기능으로 출력합니다.
5. 출력이 끝나면 휴대폰에서 **전송 취소 · PDF 삭제**를 눌러 PDF를 즉시 삭제할 수 있습니다.

PDF는 업로드 후 10분간 열 수 있습니다. 별도의 개인 계정 로그인 없이 자료를 전달할 수 있습니다.

## 3. 주요 기능

| 기능 | 설명 |
| --- | --- |
| 단축 URL 생성 | 원본 URL을 등록하고 숫자 8자리 키를 발급 |
| 정보 조회 | 단축 키로 원본 주소와 이동 횟수 조회 |
| 리다이렉트 | 원본 주소로 HTTP 301 응답 및 이동 횟수 증가 |
| PDF 임시 전송 | 최대 10MB의 PDF를 서버에 임시 저장하고 단축 키 발급 |
| 자동 만료 및 삭제 | 10분 후 접근 차단, 1분 간격의 스케줄러로 만료 파일 정리 |
| 전송 취소 | 발급 시 받은 별도의 취소 토큰으로 PDF 즉시 삭제 |
| 웹 UI | 모바일·PC 반응형 화면, 코드 복사, 자료 새 탭 열기 및 오류 안내 |

10분 만료와 전송 취소는 PDF 전송에 적용됩니다. 일반 웹 링크는 기존 단축 URL로 이용할 수 있습니다.

## 4. 기술 스택

| 구분 | 기술 |
| --- | --- |
| Backend | Java 21, Spring Boot 4.1.2, Spring MVC, Bean Validation |
| Persistence | Spring Data JPA, Hibernate, MySQL 8.0 |
| Frontend | HTML, CSS, JavaScript |
| Test | JUnit Jupiter, Mockito, Spring Boot 통합 테스트, Postman |
| Build / Deploy | Gradle Wrapper, Docker, Railway |

## 5. 프로젝트 구조

```text
shorten-url-service/
├── Dockerfile
├── build.gradle
├── src/
│   ├── main/
│   │   ├── java/com/example/shorten_url_service/
│   │   │   ├── presentation/    # Controller, 요청·응답 DTO, 예외 처리
│   │   │   ├── application/     # URL 생성·조회·리다이렉트, PDF 업로드
│   │   │   ├── domain/          # ShortenUrl, 저장소 인터페이스, 도메인 예외
│   │   │   └── infrastructure/  # JPA 저장소, 엔티티, PDF 파일 저장소
│   │   └── resources/
│   │       ├── application.properties
│   │       ├── application-deploy.properties
│   │       ├── db/deploy-schema.sql
│   │       └── static/print/    # 웹 화면
│   └── test/
└── README.md
```

### JPA 저장소 전환

기존 `ConcurrentHashMap` 기반 저장소에서 JDBC를 거쳐 Spring Data JPA 기반 MySQL 저장소로 전환했습니다. Service는 `ShortenUrlRepository` 인터페이스에 의존하고, `DatabaseShortenUrlRepository`가 도메인 객체와 `ShortenUrlEntity`를 변환해 `ShortenUrlJpaRepository`에 저장과 조회를 위임합니다.

MySQL의 `shorten_urls` 테이블에는 다음 데이터를 저장합니다.

| 열 | 역할 |
| --- | --- |
| `id` | 자동 증가 기본 키 |
| `original_url` | 원본 웹 URL 또는 서버의 PDF 경로 |
| `shorten_url_key` | 숫자 8자리 문자열, UNIQUE |
| `redirect_count` | 이동 횟수 |

단축 키는 앞자리 0을 보존하도록 문자열로 관리합니다. 원본 URL과 이동 횟수는 애플리케이션을 재시작해도 DB에 남습니다.

## 6. 트랜잭션 및 동시성 설계

| 기능 | 적용 위치 | 이유 |
| --- | --- | --- |
| URL 생성 | `DatabaseShortenUrlRepository.insertShortenUrl()` | 신규 저장 시도마다 커밋 또는 롤백할 수 있도록 범위를 설정 |
| 정보 조회 | `SimpleShortenUrlService.getShortenUrlInformationByShortenUrlKey()` | `@Transactional(readOnly = true)`로 조회 작업의 목적을 표현 |
| 리다이렉트 | `SimpleShortenUrlService.getOriginalUrlByShortenUrlKey()` | 원본 주소 조회와 이동 횟수 증가를 하나의 쓰기 트랜잭션으로 처리 |

### 단축 키 중복 방지

저장 전 키가 이미 존재하는지 확인하고, DB에도 UNIQUE 제약을 적용했습니다. 동시에 같은 키를 생성한 요청이 있으면 UNIQUE 제약이 중복 INSERT를 거절합니다.

키 생성 Service는 트랜잭션 밖에서 반복문을 실행합니다. 현재 API의 호출 구조에서는 저장 시도별 트랜잭션을 종료하고, 무결성 예외가 발생하면 DB를 재조회해 해당 키의 존재 여부로 충돌을 판단합니다. 키가 존재하면 다음 후보를 시도하며, 키가 없으면 발생한 예외를 호출자에게 전달합니다. 전체 키 후보는 최대 5개입니다. 이 방식으로 실패한 저장의 롤백 상태가 다음 시도에 이어지는 상황을 피했습니다.

### 이동 횟수 갱신

여러 요청이 동시에 기존 횟수를 읽고 각각 같은 값을 저장하면 증가가 누락될 수 있습니다.

`ShortenUrlJpaRepository`에서는 JPQL의 `redirectCount = redirectCount + 1` 연산으로 DB에 저장된 값을 직접 증가시켜 갱신 손실을 방지했습니다.

## 7. PDF 저장·만료·취소 설계

- PDF 파일 자체는 서버의 JVM 임시 폴더에 저장하고, 파일을 여는 상대 경로를 기존 단축 URL 구조에 연결합니다.
- 빈 파일, 10MB 초과 파일, PDF 헤더가 일치하지 않는 파일을 거절합니다.
- 업로드 중에는 임시 `.part` 파일을 사용합니다. 파일 저장 후 단축 키 발급에 실패하면 해당 파일을 정리합니다.
- 저장 완료 시각을 기준으로 10분이 지나면 단축 키 조회, 리다이렉트, PDF 직접 접근을 모두 차단합니다. 스케줄러가 1분 간격으로 만료된 파일을 삭제합니다.
- PDF 업로드 응답에서 취소 토큰을 함께 발급합니다. 서버에는 토큰의 SHA-256 해시를 보관하고, 올바른 토큰으로 요청한 경우에만 즉시 삭제합니다. 숫자 8자리 코드 조회 응답에는 취소 토큰이 포함되지 않습니다.
- 취소하거나 만료된 PDF의 키는 DB에 남겨 다른 자료에 동일한 키가 재사용되지 않도록 합니다. 이후 접근 시 404를 반환합니다.

PDF는 임시 파일이므로 서버 재배포나 재시작으로 사라질 수 있습니다. 코드를 아는 사람은 유효 시간 동안 자료를 열 수 있으므로 공개 데모에서는 개인정보가 없는 자료를 사용해 주세요. 이미 다운로드한 PDF는 서버에서 파일을 삭제해도 기기에 남을 수 있습니다.

## 8. API

| Method | URI | 기능 | 주요 응답 |
| --- | --- | --- | --- |
| GET | `/` | 서비스 화면 | 200 |
| POST | `/shortenUrl` | URL 등록 (JSON) | 200, 400 |
| GET | `/shortenUrl/{shortenUrlKey}` | 정보 조회 | 200, 404 |
| GET | `/{shortenUrlKey}` | 원본 주소 이동 | 301, 404 |
| POST | `/pdf` | PDF 등록 (multipart/form-data의 `file`) | 200, 400, 413 |
| GET | `/pdf/{fileName}` | PDF 열기 | 200, 404 |
| DELETE | `/pdf/{fileName}` | PDF 전송 취소 (`X-Cancel-Token` 헤더) | 204, 400, 404 |

### 단축 URL 생성 예시

`POST /shortenUrl`

원본 URL은 입력값과 URI 인코딩 후의 길이가 각각 2,048자 이하여야 합니다. 한도를 넘으면 코드 발급 전에 HTTP 400을 반환합니다.

```json
{
  "originalUrl": "https://www.hanbit.co.kr/"
}
```

응답 예시

```json
{
  "originalUrl": "https://www.hanbit.co.kr/",
  "shortenUrlKey": "01234567"
}
```

## 9. 로컬 실행 방법

### 사전 준비

- JDK 21
- MySQL 서버 (Docker 사용 가능)
- MySQL에서 `shorten_url_service` 데이터베이스와 `shorten_app` 계정 준비
- `src/main/resources/db/deploy-schema.sql`을 해당 데이터베이스에 실행하여 테이블 생성

기본 로컬 연결 설정은 다음과 같습니다.

| 항목 | 설정 |
| --- | --- |
| JDBC URL | `jdbc:mysql://localhost:3307/shorten_url_service` |
| 사용자 | `shorten_app` |
| 비밀번호 | 환경 변수 `SHORTEN_URL_DB_PASSWORD` |

`shorten_app`에는 이 테이블을 조회·추가·수정할 권한이 필요합니다. MySQL이 다른 포트를 사용하면 `application.properties`의 접속 주소를 실행 환경에 맞춰 설정합니다.

### 실행

프로젝트의 `shorten-url-service` 디렉터리에서 PowerShell을 열고 실행합니다. MySQL 서버는 실행 상태여야 합니다.

```powershell
$password = Read-Host "MySQL 비밀번호" -AsSecureString
$env:SHORTEN_URL_DB_PASSWORD = [System.Net.NetworkCredential]::new('', $password).Password
.\gradlew.bat bootRun
```

브라우저에서 [http://localhost:8080/](http://localhost:8080/)에 접속합니다.

### 테스트

동일한 환경 변수 및 MySQL 설정을 사용합니다.

```powershell
.\gradlew.bat test
```

## 10. 검증 결과

- 로컬 MySQL을 실행한 환경에서 JUnit 테스트 21개 통과 (실패 0개, 건너뜀 0개)
- 웹 테스트 4개로 영문·한글 URL 길이 경계값, 8자리 코드 경로, 301 응답의 Location 및 Cache-Control 헤더를 검증
- 실제 MySQL 환경에서 URL 생성·조회·중복 제약 및 기존 링크 보존 검증
- 키 충돌 후 재시도와 이동 횟수 증가 검증
- 잘못된 단축 키, 빈 파일, 잘못된 PDF 형식, 파일 크기 제한 검증
- PDF 만료 후 접근 차단 및 스케줄러의 실제 파일 삭제 검증
- 취소 토큰 검증, 즉시 파일 삭제, 취소된 링크 접근 차단 검증
- 실제 아이폰에서 PDF를 업로드하고 PC에서 같은 파일을 열어 인쇄 미리보기 확인
- 배포된 HTTPS 주소에서 PDF 업로드·열기, 전송 취소 및 10분 후 만료 확인

## 11. 배포

Railway에서 Java 21 Docker 컨테이너와 MySQL 서비스를 실행합니다. `deploy` 프로필은 환경 변수로 DB 접속 정보를 전달받고, 스키마 초기화 SQL을 적용한 후 JPA 매핑을 검증합니다.

[프린트 링크 공개 데모](https://ezprint.up.railway.app/)

현재 발표용 무료 체험으로 운영하며, 제공되는 크레딧이나 체험 기간이 종료되면 서비스 이용이 중단될 수 있습니다.
