# 옷 위시리스트와 예산 추천

사고 싶은 옷을 등록하고, 예산 안에서 살 수 있는 옷을 최대 3개 추천하는 프로젝트입니다.

- 주제: 옷 위시리스트
- 환경: Java 17, Spring Boot 4.1.2, Gradle Wrapper
- 실행: `personal-project/`에서 `./gradlew bootRun` (Windows는 `.\gradlew.bat bootRun`)
- 테스트: `./gradlew test`
- 화면: http://localhost:8080/

## 구조

| 계층 | 클래스 | 역할 |
| --- | --- | --- |
| domain | `CollectionItem` | `id`, `name`, `price`와 입력 규칙(검증 어노테이션) |
| domain | `CollectionItemRepository` | 저장소 인터페이스 |
| domain | `EntityNotFoundException` | 없는 id를 구분하는 예외 |
| infrastructure | `ListCollectionItemRepository` | 메모리(List) 구현 |
| application | `SimpleCollectionItemService` | 등록, 조회, 수정, 삭제, 추천 (생성자로 Repository 주입) |
| application | `ValidationService` | `CollectionItem` 검증 실행 |
| presentation | `CollectionItemController` | REST API |

## 규칙

- 이름: `null`, 빈 문자열, 공백만 있는 이름은 거부하고 앞뒤 공백은 제거합니다. 최대 100자입니다.
- 가격: 0원 이상 1억 원 이하의 정수입니다. 0원(무료)을 허용합니다.
- id: 등록할 때 프로그램이 1부터 부여하며, 삭제해도 다시 쓰지 않습니다.
- 없는 id를 조회, 수정, 삭제하면 `EntityNotFoundException`이 발생합니다.
- 등록이나 수정에 실패하면 기존 목록과 값은 바뀌지 않습니다.
- 추천: 예산은 0 이상이어야 합니다. 가격이 예산 이하인 옷을 가격 오름차순으로 최대 3개 반환하고, 같은 가격이면 등록 순서를 유지합니다. 후보가 없으면 빈 목록을 반환하며, 원본 목록은 바뀌지 않습니다.

## 완료한 기능

- [x] 위시리스트에 아이템 등록
- [x] 전체 목록과 등록한 id의 아이템 조회
- [x] 예산 안에서 살 수 있는 아이템 최대 3개 추천
- [x] Repository 인터페이스와 메모리 구현 분리
- [x] Service는 생성자로 Repository 주입
- [x] 추가: 수정, 삭제, REST API, 화면(`index.html`), 이름 100자·가격 1억 원 상한

## API

| 요청 | 요청 값 | 성공 응답 | 실패 응답 |
| --- | --- | --- | --- |
| `POST /collectionItems` | `{"name":"흰 티셔츠","price":15000}` | `{"id":1,"name":"흰 티셔츠","price":15000}` | 입력 규칙 위반 시 400 |
| `GET /collectionItems` | | 등록 순서대로 전체 목록 | |
| `GET /collectionItems/{id}` | | 아이템 1개 | 없는 id는 404 |
| `PUT /collectionItems/{id}` | `{"name":"롱코트","price":150000}` | 수정된 아이템 | 400, 404 |
| `DELETE /collectionItems/{id}` | | 200 | 없는 id는 404 |
| `GET /collectionItems/recommendations` | `budget=15000` | 추천 목록 (최대 3개) | 음수 예산은 400 |

## 테스트 결과

`./gradlew test`: `BUILD SUCCESSFUL`, 22개 통과

| 테스트 | 개수 | 확인 내용 |
| --- | --- | --- |
| `SimpleCollectionItemServiceUnitTest` (Mockito) | 6 | 등록 반환값, 예산 경계값, 가격 정렬과 같은 가격의 순서, 최대 3개, 빈 결과, 원본 보존 |
| `SimpleCollectionItemServiceTest` (`@SpringBootTest`) | 15 | 등록·목록·단건 조회, 없는 id, 공백 제거, 빈 이름·음수 가격·음수 예산 거부, 실패 후 목록 유지, 가격 0, 예산 0, 추천 결과, 원본 보존, 수정·삭제 |
| `DemoApplicationTests` | 1 | 애플리케이션 실행 |

## 실행 결과

화면에서 코트 120,000원, 증정 양말 0원, 흰 티셔츠 15,000원, 줄무늬 티셔츠 15,000원, 청바지 50,000원을 순서대로 등록해 확인했습니다.

| 입력 | 기대 결과 | 실제 결과 | 통과 |
| --- | --- | --- | --- |
| 위 5개 등록 | id 1~5 부여 | `'증정 양말'을(를) id 2로 등록했습니다.` 등 | O |
| 이름에 스페이스만 3칸 | 거부, 목록 유지 | 400, 목록 5개 유지 | O |
| 가격 `-1` | 거부 | 400 | O |
| 예산 `15000` | 증정 양말, 흰 티셔츠, 줄무늬 티셔츠 | 1 증정 양말 0원, 2 흰 티셔츠 15,000원, 3 줄무늬 티셔츠 15,000원 | O |
| 예산 `-5` | 거부 | 400, `예산은 0 이상이어야 합니다.` | O |
| id `2` 조회 | 증정 양말 | `id 2 · 증정 양말 · 0원` | O |
| id `99` 조회 | 찾을 수 없음 | 404, `해당 id의 옷을 찾을 수 없습니다.` | O |
| id 1을 빈 이름으로 수정 | 거부 | 400, 입력 규칙 안내 문구 표시 | O |
| id 1을 롱코트 150,000원으로 수정 | 수정 | `id 1을(를) 수정했습니다.` | O |
| 청바지 삭제 | 삭제 | `'청바지'을(를) 삭제했습니다.`, 목록 4개 | O |
