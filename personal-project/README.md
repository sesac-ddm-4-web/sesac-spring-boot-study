# 취미 위시리스트와 예산 추천

사고 싶은 책·문구·게임·캠핑용품 등을 등록하고, 예산 안에서 구매할 수 있는 아이템을 추천받는 Spring Boot 프로젝트입니다.

개인 실습 저장소의 `personal-project/`에 구현했습니다. 백엔드는 직접 작성하고, 프론트엔드는 AI를 활용해 순수 HTML·CSS·JavaScript로 작성했습니다.

## 개발 환경

| 항목 | 환경 |
|---|---|
| 언어 | Java 21 |
| 프레임워크 | Spring Boot 4.1.2 |
| 빌드 | Gradle Wrapper, Groovy DSL |
| 테스트 | JUnit 5, Mockito, Spring Boot Test |
| 프론트엔드 | HTML·CSS·JavaScript, Fetch API |
| 저장 방식 | 메모리 저장소 |
| 서버 포트 | 8081 |

외부 프론트엔드 라이브러리나 CDN을 사용하지 않습니다. 별도 데이터베이스 설정은 필요하지 않습니다.

## 실행 방법

JDK 21을 준비하고, 저장소의 `personal-project` 폴더에서 실행합니다.

### Windows PowerShell

```powershell
cd personal-project
.\gradlew.bat bootRun
```

이미 `personal-project` 폴더 안이라면 `cd`는 생략합니다.

### macOS / Linux

```bash
cd personal-project
chmod +x gradlew
./gradlew bootRun
```

실행 후 브라우저에서 [http://localhost:8081/](http://localhost:8081/)에 접속합니다. 프론트 파일을 직접 열지 않고 Spring Boot 서버를 통해 접속해야 API를 사용할 수 있습니다.

> 데이터는 메모리에 저장되므로 서버를 재시작하면 초기화됩니다. 처음 실행할 때는 빈 목록이 표시되며, 화면에서 아이템을 등록해 사용할 수 있습니다.

## 완료 기능

### 등록·조회

- 아이템 등록, 전체 목록 조회, id로 단건 조회
- null·빈 문자열·공백뿐인 이름 거부, 이름 앞뒤 공백 제거
- 가격은 0 이상의 정수이며 무료 아이템 0원 허용
- 등록 요청의 id 대신 프로그램이 생성한 id 부여
- 등록 결과로 받은 실제 id로 조회
- 없는 id는 `EntityNotFoundException`으로 구분
- 입력 검증을 통과한 뒤 저장하여 실패한 등록이 기존 목록을 변경하지 않도록 처리

### 예산 추천

- 0원 이상 예산 허용, 음수 예산 거부
- **개별 가격이 예산 이하**인 아이템을 선택
- 가격 오름차순 정렬, 같은 가격은 등록 순서 유지
- 정렬한 결과에서 최대 3개 반환
- 조건에 맞는 아이템이 없으면 빈 목록 반환
- 추천 조회 시 원본 목록의 내용과 순서 유지

추천한 아이템들의 합계를 예산 안에 맞추는 방식은 아닙니다.

### 추가 과제: 이름 부분 검색

- 이름에 검색어가 포함된 아이템 조회
- 검색어 앞뒤 공백 제거
- null·빈 문자열·공백뿐인 검색어 거부
- 검색 결과가 없으면 빈 목록 반환
- 검색 결과의 등록 순서 유지

### 프론트엔드

- 아이템 등록 폼과 전체 목록
- 이름 부분 검색과 전체 보기
- 예산 입력과 추천 결과 표시
- 요청 중 버튼 비활성화 및 요청 실패 안내
- 모바일 화면 대응
- 샘플 목록이 아닌 실제 HTTP API 응답을 표시

브라우저의 필수 입력 검사와 JavaScript 입력 검사를 사용하며, 서버에서도 별도로 입력을 검증합니다.

## 프로젝트 구조

```text
personal-project/
├── build.gradle
├── settings.gradle
├── gradlew / gradlew.bat
├── gradle/wrapper/
└── src/
    ├── main/
    │   ├── java/com/example/personal_project/
    │   │   ├── PersonalProjectApplication.java
    │   │   ├── application/
    │   │   │   ├── ItemService.java
    │   │   │   └── ValidationService.java
    │   │   ├── domain/
    │   │   │   ├── Item.java
    │   │   │   ├── ItemRepository.java
    │   │   │   └── EntityNotFoundException.java
    │   │   ├── infrastructure/
    │   │   │   └── MemoryItemRepository.java
    │   │   └── presentation/
    │   │       ├── ItemController.java
    │   │       └── ItemDto.java
    │   └── resources/
    │       ├── application.properties
    │       └── static/
    │           ├── index.html
    │           ├── style.css
    │           └── app.js
    └── test/java/com/example/personal_project/
        ├── PersonalProjectApplicationTests.java
        └── application/
            ├── ItemServiceTest.java
            └── ItemServiceUnitTest.java
```

과제의 `CollectionItem` 모델은 코드에서 `Item`이라는 이름으로 구현했습니다. 필드는 `Long id`, `String name`, `int price`입니다.

`ItemRepository` 인터페이스와 `MemoryItemRepository` 구현을 분리했습니다. `ItemService`는 생성자로 Repository와 ValidationService를 주입받습니다. Service에서 DTO 변환을 담당하고, ValidationService에서 이름·가격·예산 검증을 담당합니다.

메모리 저장소는 `CopyOnWriteArrayList`로 등록 순서를 보관하고, `AtomicLong`으로 id를 생성합니다. 전체 조회 시 목록을 복사해 반환합니다.

## HTTP API

기본 주소: `http://localhost:8081`

| 기능 | 메서드 | 경로 |
|---|---|---|
| 등록 | POST | `/items` |
| 전체 조회 | GET | `/items` |
| 단건 조회 | GET | `/items/{id}` |
| 예산 추천 | GET | `/items/recommend?budget=5000` |
| 이름 부분 검색 | GET | `/items/search?name=노트` |

### 등록 요청·응답 예시

`POST /items`에 `Content-Type: application/json`으로 요청합니다.

```json
{
  "name": "  노트  ",
  "price": 12000
}
```

첫 등록의 응답 예시입니다. id는 서버 상태에 따라 달라지므로 응답으로 받은 값을 사용합니다.

```json
{
  "id": 1,
  "name": "노트",
  "price": 12000
}
```

위 응답을 받았다면 `GET /items/1`로 조회합니다.

### 추천 동작 예시

아래 순서로 등록했을 때의 기대 결과입니다.

| 등록 순서 | 이름 | 가격 |
|---|---|---:|
| 1 | 노트 | 12000 |
| 2 | 무료 샘플 | 0 |
| 3 | 펜 | 5000 |
| 4 | 스티커 | 5000 |
| 5 | 게임 | 8000 |

- 예산 5000원 → 무료 샘플, 펜, 스티커
- 예산 0원 → 무료 샘플
- 예산 8000원 → 무료 샘플, 펜, 스티커 (최대 3개)

### 예외 처리 범위

잘못된 이름·가격·예산은 `IllegalArgumentException`, 없는 id는 `EntityNotFoundException`을 발생시킵니다. 별도 공통 예외 핸들러는 구현하지 않았으므로 해당 예외를 HTTP 400·404로 변환하지는 않습니다. 서버 검증 실패 시 HTTP 500으로 응답할 수 있습니다.

## 테스트

`personal-project` 폴더에서 실행합니다.

```powershell
.\gradlew.bat test
```

macOS / Linux에서는 `./gradlew test`를 사용합니다.

### 단위 테스트 — ItemServiceUnitTest

Mockito의 `@Mock`, `@InjectMocks`로 Service의 동작을 확인합니다.

1. 정상 등록 결과 반환
2. 이름 검증 실패 시 Repository 호출 방지
3. 예산 0원일 때 무료 아이템 추천
4. 예산 경계 포함, 가격 정렬, 같은 가격의 순서, 최대 3개 제한
5. 추천 후보가 없을 때 빈 목록 반환

### 통합 테스트 — ItemServiceTest

`@SpringBootTest`와 `@Autowired`를 사용해 Spring이 연결한 실제 Service·ValidationService·메모리 Repository를 확인합니다.

1. 정상 등록, 이름 공백 제거, 실제 id 조회
2. 없는 id 조회 시 예외
3. null 이름 거부
4. 빈 이름 거부
5. 공백 이름 거부
6. 음수 가격 거부
7. 등록 실패 후 기존 목록 보존
8. 음수 예산 거부
9. 추천 조회 전후 원본 목록 보존
10. 검색어 공백 제거 및 이름 부분 검색
11. 빈 검색어 거부
12. 공백 검색어 거부
13. 검색 결과가 없을 때 빈 목록 반환

### 확인한 실제 결과

작성 시점의 로컬 Gradle 테스트 결과 XML 기준입니다.

| 테스트 클래스 | 실행 | 실패 | 오류 | 건너뜀 |
|---|---:|---:|---:|---:|
| ItemServiceUnitTest | 5 | 0 | 0 | 0 |
| ItemServiceTest | 13 | 0 | 0 | 0 |
| 합계 | 18 | 0 | 0 | 0 |

기본 생성된 `PersonalProjectApplicationTests`는 위 집계에 포함하지 않았습니다. 이 README 작성 과정에서 테스트를 재실행한 것은 아니며, 기존 실행 결과를 확인했습니다.

테스트 실행 후 상세 보고서는 `build/reports/tests/test/index.html`에서 확인할 수 있습니다.
