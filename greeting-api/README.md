# 인사 API와 스터디 참가 신청

Spring Boot로 인사말을 반환하는 기본 실습과 스터디 참가 신청을 관리하는 심화 실습입니다.

## 실행 환경과 방법

- Java 21 (검증 환경 Temurin 21.0.12.1)
- Spring Boot 4.1.2
- Gradle Wrapper 9.8.2

저장소 루트에서 다음 명령을 실행

```powershell
cd greeting-api
.\gradlew.bat build
.\gradlew.bat bootRun
```

서버를 시작한 뒤 [스터디 참가 신청 화면](http://localhost:8080/study.html)을 열기

## 기본 인사 API

| 요청 | 기대 응답 | 실제 응답 | 결과 이미지|
| --- | --- | --- | --- |
| `GET /hello` | 200 · `Hello, Spring!` | 200 · `Hello, Spring!` | [API 응답](docs/images/hello-response.png) |
| `GET /greet?name=민지` | 200 · `안녕하세요, 민지님` | 200 · `안녕하세요, 민지님` | [API 응답](docs/images/greet-named-response.png) · [브라우저 Network](docs/images/greet-named-network.png) |
| `GET /greet` | 200 · `안녕하세요, 방문자님` | 200 · `안녕하세요, 방문자님` | [API 응답](docs/images/greet-default-response.png) |
| `GET /not-found` | 404 | 404 · JSON 본문에 `status: 404`, `error: "Not Found"`, `path: "/not-found"` | [API 응답](docs/images/not-found-response.png) · [브라우저 Network](docs/images/not-found-network.png) |

## 스터디 참가 신청 API

| 요청 | 입력 | 응답 |
| --- | --- | --- |
| `POST /study/participants` | `name` | 신청 결과와 참가자 ID·이름·상태 |
| `GET /study/participants` | 없음 | 신청 순서대로 한 사람씩 한 줄에 표시. 빈 명단은 `참가자가 없습니다.` |
| `POST /study/cancel` | `memberId` | 취소한 참가자와 승격된 대기자 정보 |
| `POST /study/capacity` | `capacity` | 유효한 입력의 처리 결과와 현재 정원. 입력 검증 오류에는 거절 이유 표시 |

명단의 각 행은 `[ id=0, 이름=A, 상태=확정 ]` 형식입니다. 신청 결과에서 받은 ID를 취소에 사용합니다.

## Controller와 Service의 역할

- `HelloController`는 인사 요청을 받고 `GreetingService`는 이름을 포함한 인사말을 만듭니다.
- `StudyController`는 입력을 받아 검사하고, 생성자로 주입받은 `StudyService`에 처리를 맡깁니다.
- `StudyService`는 정원과 명단을 메모리에 보관합니다. 서버를 재시작하면 정원은 3명으로 돌아가고 명단은 비워집니다.
- 정원이 차면 새 신청자는 대기합니다. 확정자가 취소하면 첫 대기자 1명을, 정원을 늘리면 빈자리만큼 대기자를 신청 순서대로 확정합니다. 대기자 취소는 다른 참가자의 상태에 영향을 주지 않습니다.

## QA 체크리스트

| 사전 조건 | 입력·동작 | 기대 결과 | 실제 결과 | 통과 여부 | 결과 이미지 |
| --- | --- | --- | --- | --- | --- |
| 서버 실행 중 | `GET /study.html` | 정적 페이지 응답 | 200 · HTML 응답 | 통과 | [브라우저 Network](docs/images/study-page-network.png) |
| 서버 시작 직후 | 명단 조회 | 빈 명단 안내 | 200 · `참가자가 없습니다.` | 통과 | [빈 명단](docs/images/study-empty-list.png) |
| 초기 정원 3명 | A~E 순서대로 신청 | A~C 확정, D·E 대기 | 200 · ID 0~4가 발급되고 상태가 기대와 일치 | 통과 | [신청 및 대기 순서](docs/images/study-registration-waitlist.png) |
| A 등록 완료 | 앞뒤에 공백을 붙인 A 신청 | 중복 거절 | 200 · `명단에 이미 있는 이름입니다.` | 통과 | [중복 거절](docs/images/study-duplicate-rejected.png) |
| D·E 대기 중 | A의 ID 0 취소 | A 삭제, D 승격 | 200 · A 취소 및 D 승격 표시 | 통과 | [취소 및 승격](docs/images/study-cancel-and-promote.png) |
| 정원 3명, E 대기 중 | 정원을 5명으로 변경 | E 승격 | 200 · 정원 5명 및 E 승격 표시 | 통과 | [정원 확대 및 승격](docs/images/study-capacity-increase.png) |
| 정원 5명, 확정 4명 | 정원 2와 11을 차례로 요청하고 명단 조회 | 두 요청 거절, 기존 명단 유지 | 2는 현재 정원 5명과 확정 4명을 안내하고, 11은 허용 범위를 안내함. 명단에는 B~E가 확정으로 표시됨 | 통과 | [확정 인원 미만](docs/images/study-capacity-below-confirmed.png) · [범위 초과](docs/images/study-capacity-out-of-range.png) |
| 정원 5명, B~E 확정, 대기자 없음 | B를 취소한 뒤 같은 이름으로 다시 신청 | 취소 시 승격 없이 B만 삭제, 재신청 시 새 ID로 확정 | ID 1의 B가 삭제되고 ID 5로 재신청되어 확정됨 | 통과 | [취소](docs/images/study-cancel-without-waitlist.png) · [재신청](docs/images/study-reregister-after-cancel.png) |

