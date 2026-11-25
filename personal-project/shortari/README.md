# 🎮 Shortari — 나만의 단축 URL 서비스

> **긴 URL은 짧게, 자주 사용하는 링크는 이름으로 간편하게!**

Shortari는 긴 URL을 짧은 주소로 변환하고, 사용자가 원하는 이름을 지정하여 링크를 관리할 수 있는 **URL 단축 및 링크 관리 서비스**입니다.

단순한 URL 단축 기능에서 나아가, 자주 사용하는 사이트를 이름으로 검색하고 바로 이동할 수 있도록 구현했습니다.

프론트엔드는 게임을 연상시키는 UI/UX를 적용하여 직관적이고 재미있게 사용할 수 있도록 구성했습니다.

---

## 📌 프로젝트 개요

| 항목 | 내용 |
|---|---|
| 프로젝트명 | Shortari |
| 프로젝트 유형 | 개인 미니 프로젝트 |
| 개발 형태 | 풀스택 웹 애플리케이션 |
| 주요 기능 | URL 단축, URL 네이밍, 리다이렉트, 이름 검색, 링크 관리 |
| Backend | Java 21, Spring Boot |
| Frontend | React, Vite |
| Database | MySQL |
| 테스트 | JUnit 5, Mockito, Spring Boot Test |
| 개발 도구 | IntelliJ IDEA, VS Code, MySQL Workbench, Gradle |

### 🎯 프로젝트 목표

- 긴 URL을 짧은 URL로 변환하는 서비스 구현
- 랜덤 문자열을 활용한 단축 코드 생성
- URL 네이밍 기능을 통한 링크 관리
- Spring Boot와 MySQL을 활용한 데이터 저장 및 조회
- REST API를 활용한 프론트엔드·백엔드 통신
- 예외 처리 및 데이터베이스 제약조건 적용
- 단위 테스트와 컨트롤러 테스트를 통한 기능 검증

---

## 🛠️ 기술 스택

### ☕ Backend

![Java](https://img.shields.io/badge/Java%2021-ED8B00?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-6DB33F?style=for-the-badge&logo=springboot&logoColor=white)
![Spring JDBC](https://img.shields.io/badge/Spring%20JDBC-6DB33F?style=for-the-badge&logo=spring&logoColor=white)
![Gradle](https://img.shields.io/badge/Gradle-02303A?style=for-the-badge&logo=gradle&logoColor=white)

- **Java 21** : 백엔드 비즈니스 로직 구현
- **Spring Boot** : REST API 및 웹 애플리케이션 개발
- **Spring JDBC** : SQL 기반 데이터베이스 접근
- **Gradle** : 프로젝트 빌드 및 의존성 관리

### ⚛️ Frontend

![React](https://img.shields.io/badge/React-20232A?style=for-the-badge&logo=react&logoColor=61DAFB)
![JavaScript](https://img.shields.io/badge/JavaScript-F7DF1E?style=for-the-badge&logo=javascript&logoColor=black)
![Vite](https://img.shields.io/badge/Vite-646CFF?style=for-the-badge&logo=vite&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css&logoColor=white)

- **React** : 컴포넌트 기반 사용자 인터페이스 구현
- **JavaScript** : 사용자 이벤트 및 API 통신 처리
- **Vite** : 프론트엔드 개발 서버 및 빌드 도구
- **CSS** : 게임 컨셉의 UI/UX 구현

### 🗄️ Database

![MySQL](https://img.shields.io/badge/MySQL-4479A1?style=for-the-badge&logo=mysql&logoColor=white)

- **MySQL** : 원본 URL, 단축 코드, 표시 이름 등 데이터 저장
- **MySQL Workbench** : 테이블 구조 및 저장 데이터 확인
- **UNIQUE 제약조건** : 단축 코드 및 표시 이름의 중복 방지
- **CHECK 제약조건** : URL 생성 방식의 허용 값 제한

### 🧪 Testing & Tools

![JUnit5](https://img.shields.io/badge/JUnit5-25A162?style=for-the-badge&logo=junit5&logoColor=white)
![Mockito](https://img.shields.io/badge/Mockito-78A641?style=for-the-badge)
![IntelliJ](https://img.shields.io/badge/IntelliJ%20IDEA-000000?style=for-the-badge&logo=intellijidea&logoColor=white)
![Git](https://img.shields.io/badge/Git-F05032?style=for-the-badge&logo=git&logoColor=white)
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)

- **JUnit 5** : 단위 테스트 작성
- **Mockito** : Repository 등 의존 객체를 대체한 테스트
- **Spring Boot Test** : 애플리케이션 컨텍스트 검증
- **Gradle Test** : 테스트 자동 실행 및 HTML 결과 보고서 생성
- **Git / GitHub** : 소스 코드 버전 관리

---

## ✨ 주요 기능

### 🔗 1. 단축 URL 자동 생성 (SHORT)

사용자가 입력한 긴 URL을 랜덤한 8자리 문자열이 포함된 짧은 URL로 변환합니다.

**동작 과정**

1. 사용자가 원본 URL 입력
2. URL 형식 검증 및 정규화
3. `SecureRandom`을 이용하여 8자리 단축 코드 생성
4. MySQL에 원본 URL과 단축 코드 저장
5. 생성된 단축 URL을 사용자에게 반환

**예시**

```text
원본 URL
https://www.naver.com

단축 코드
aB3xK912

단축 URL
http://localhost:8080/r/aB3xK912
```

단축 코드는 영문 대문자, 소문자, 숫자로 구성된 **Base62 문자 집합**을 사용합니다.

```text
0~9   : 10개
A~Z   : 26개
a~z   : 26개

총 62개 문자
```

8자리 단축 코드를 생성하므로 가능한 조합 수는 다음과 같습니다.

```text
62^8 = 218,340,105,584,896
```

단축 코드는 무작위로 생성되기 때문에 중복 가능성이 완전히 사라지는 것은 아닙니다.

이를 방지하기 위해 MySQL의 UNIQUE 제약조건을 적용하여 중복 코드 저장을 차단합니다.

### 🏷️ 2. URL 네이밍 (NAMING)

사용자가 원하는 이름을 지정하여 URL을 등록하고 관리할 수 있습니다.

**예시**

```text
원본 URL : https://www.naver.com
표시 이름 : 네이버
단축 코드 : PTAuhvCh
```

- 사용자 지정 이름 등록
- 이름이 있는 링크끼리 중복 등록 방지
- 이름이 없는 링크도 등록 가능
- 링크 목록에서 등록된 이름 표시
- 등록한 이름 수정 가능

표시 이름은 링크를 식별하기 위한 별명이며, 실제 리다이렉트는 단축 URL을 통해 처리합니다.

### 🔎 3. 이름으로 바로가기

Shortari 내부에서 등록된 이름을 검색하여 해당 웹사이트로 바로 이동할 수 있습니다.

**동작 과정**

```text
Shortari 검색창
        ↓
"네이버" 입력
        ↓
등록된 링크 목록에서 이름 검색
        ↓
해당 링크의 단축 URL 확인
        ↓
네이버 홈페이지 이동
```

현재는 React에서 조회한 링크 목록을 기준으로 표시 이름을 정확히 비교하는 방식입니다.

따라서 별도의 백엔드 검색 API를 추가하지 않고 기존 링크 목록 조회 API를 활용합니다.

※ 이 기능은 Shortari 내부 검색창에서 동작하며, Chrome 주소창의 검색 기능을 변경하지 않습니다.

### 🚀 4. 단축 URL 리다이렉트

사용자가 생성된 단축 URL에 접속하면 서버가 해당 단축 코드에 연결된 원본 URL을 조회합니다.

**동작 과정**

```text
사용자
  ↓
http://localhost:8080/r/aB3xK912
  ↓
Spring Boot Controller
  ↓
Service
  ↓
Repository
  ↓
MySQL에서 원본 URL 조회
  ↓
HTTP Redirect 응답
  ↓
https://www.naver.com
```

사용자가 직접 원본 URL을 다시 요청할 필요 없이 브라우저가 리다이렉트 응답을 처리합니다.

### 📚 5. 링크 목록 관리

등록한 URL을 목록으로 확인하고 관리할 수 있습니다.

| 기능 | 설명 |
|---|---|
| 전체 조회 | 저장된 단축 URL 목록 확인 |
| URL 복사 | 생성된 단축 URL 클립보드 복사 |
| 링크 이동 | 단축 URL을 통해 원본 웹사이트 이동 |
| 이름 수정 | 등록한 표시 이름 변경 |
| 링크 삭제 | 등록한 URL 데이터 삭제 |
| 최신순 정렬 | 최근 생성한 URL부터 표시 |
| 오래된순 정렬 | 먼저 생성한 URL부터 표시 |
| 이름순 정렬 | 표시 이름을 기준으로 정렬 |

### 🛡️ 6. 예외 처리 및 데이터 검증

정상적인 요청뿐 아니라 잘못된 입력과 중복 데이터에 대한 예외 처리도 구현했습니다.

| 상황 | 처리 |
|---|---|
| 잘못된 URL 입력 | URL 유효성 검증 |
| 중복 표시 이름 등록 | `DuplicateDisplayNameException` |
| 존재하지 않는 단축 URL | `ShortUrlNotFoundException` |
| 단축 코드 생성 실패 | `ShortCodeGenerationException` |
| 잘못된 URL 형식 | `InvalidUrlException` |
| 단축 코드 중복 | DB UNIQUE 제약조건으로 중복 저장 방지 |

예외 클래스에는 `@ResponseStatus`를 적용하여 HTTP 상태 코드를 지정했습니다.

별도의 글로벌 예외 핸들러를 사용하지 않고 각 예외 클래스에서 상태 코드를 관리하는 구조입니다.

---

## 🏗️ 시스템 아키텍처

```text
┌──────────────────────────────┐
│          사용자              │
│       Chrome Browser         │
└──────────────┬───────────────┘
               │
               ▼
┌──────────────────────────────┐
│      React Frontend          │
│      localhost:5173          │
│                              │
│  - URL 입력                  │
│  - SHORT / NAMING            │
│  - 이름 검색                 │
│  - 링크 목록 관리            │
└──────────────┬───────────────┘
               │ HTTP / JSON
               ▼
┌──────────────────────────────┐
│      Spring Boot Backend     │
│      localhost:8080          │
│                              │
│  Controller                  │
│       ↓                      │
│  Service                     │
│       ↓                      │
│  Repository                  │
└──────────────┬───────────────┘
               │ JDBC / SQL
               ▼
┌──────────────────────────────┐
│           MySQL              │
│                              │
│       short_url 테이블       │
│                              │
│  - id                        │
│  - original_url              │
│  - short_code                │
│  - display_name              │
│  - url_mode                  │
│  - created_at                │
└──────────────────────────────┘
```

### 📂 백엔드 패키지 구조

```text
src/main/java/com/kdongd/shorturl/
│
├── controller/
│   └── ShortUrlController.java
│
├── domain/
│   ├── ShortUrl.java
│   └── UrlMode.java
│
├── dto/
│   ├── request/
│   └── response/
│
├── exception/
│   ├── DuplicateDisplayNameException.java
│   ├── InvalidUrlException.java
│   ├── ShortCodeGenerationException.java
│   └── ShortUrlNotFoundException.java
│
├── repository/
│   ├── ShortUrlRepository.java
│   └── JdbcShortUrlRepository.java
│
├── service/
│   ├── ShortCodeGenerator.java
│   ├── ShortUrlService.java
│   └── UrlNormalizer.java
│
└── ShortUrlApplication.java
```

### 📌 계층별 역할

| 계층 | 역할 |
|---|---|
| Controller | HTTP 요청 수신 및 응답 반환 |
| Service | URL 생성, 검증, 조회 등 비즈니스 로직 처리 |
| Repository | MySQL 데이터 저장 및 조회 |
| Domain | URL 데이터와 생성 방식 표현 |
| DTO | 요청 및 응답 데이터 전달 |
| Exception | 비정상적인 요청 및 처리 실패 표현 |

---

## 🗄️ 데이터베이스 설계

### short_url 테이블

| 컬럼 | 타입 | 제약조건 | 설명 |
|---|---|---|---|
| id | BIGINT | PK, AUTO_INCREMENT | URL 고유 ID |
| original_url | VARCHAR(2048) | NOT NULL | 원본 URL |
| short_code | VARCHAR(8) | NOT NULL, UNIQUE | 단축 코드 |
| display_name | VARCHAR(20) | UNIQUE, NULL 허용 | 사용자 지정 이름 |
| url_mode | VARCHAR(20) | NOT NULL, CHECK | SHORT / NAMING |
| created_at | DATETIME | NOT NULL | 생성 시간 |

### 📌 주요 설계 사항

**1. 단축 코드 중복 방지**

`short_code`에 UNIQUE 제약조건을 적용하여 서로 다른 URL이 동일한 단축 코드를 저장하지 못하도록 했습니다.

**2. 표시 이름 중복 방지**

`display_name`에 UNIQUE 제약조건을 적용했습니다.

MySQL에서는 UNIQUE 컬럼에 여러 NULL 값을 저장할 수 있으므로 이름 없는 링크는 여러 개 등록할 수 있습니다.

**3. 동일 원본 URL 등록 허용**

`original_url`에는 UNIQUE 제약조건을 적용하지 않았습니다.

따라서 서로 다른 사용자가 같은 원본 URL을 등록하더라도 각각 별도의 단축 코드를 생성할 수 있습니다.

**4. URL 생성 방식 제한**

`url_mode`는 CHECK 제약조건으로 `SHORT`, `NAMING` 값만 허용하도록 설정했습니다.

---

## 🧪 테스트 결과

### ✅ Gradle 테스트 전체 통과

| 항목 | 결과 |
|---|---|
| 전체 테스트 | **46개** |
| 성공 | **46개** |
| 실패 | **0개** |
| 건너뜀 | **0개** |
| 성공률 | **100%** |
| 실행 시간 | **1.648초** |

### 테스트 클래스별 결과

| 테스트 클래스 | 테스트 수 | 결과 |
|---|---:|---|
| ShortUrlControllerTest | 9 | ✅ 성공 |
| ShortCodeGeneratorTest | 4 | ✅ 성공 |
| ShortUrlServiceTest | 21 | ✅ 성공 |
| UrlNormalizerTest | 11 | ✅ 성공 |
| ShortUrlApplicationTests | 1 | ✅ 성공 |
| **합계** | **46** | **100% 통과** |

### 테스트 검증 영역

**ShortUrlControllerTest**
- Controller 계층의 HTTP 요청 및 응답 검증

**ShortCodeGeneratorTest**
- 단축 코드 생성 로직 검증
- 8자리 문자열 길이 및 허용 문자 검증

**ShortUrlServiceTest**
- URL 생성 및 관리에 관한 비즈니스 로직 검증

**UrlNormalizerTest**
- URL 정규화 로직 검증

**ShortUrlApplicationTests**
- Spring Boot 애플리케이션 컨텍스트 로딩 검증

테스트는 다음 명령어로 실행했습니다.

```bash
./gradlew test
```

테스트 완료 후 생성된 HTML 보고서를 통해 전체 테스트 결과를 확인했습니다.

```text
build/reports/tests/test/index.html
```

※ 46개 테스트 통과는 작성된 테스트가 모두 성공했다는 의미이며, 모든 예외 상황이나 실제 운영 환경의 동작까지 보장한다는 의미는 아닙니다.

---

## 🎮 프론트엔드 UI/UX

Shortari의 프론트엔드는 게임을 연상시키는 디자인을 적용했습니다.

### 디자인 컨셉

- 보라색 계열의 게임 테마
- 픽셀 게임을 연상시키는 배경 및 장식 요소
- 자동 생성 / 이름으로 생성 탭
- 단축 URL 생성 결과 표시
- 이름으로 바로가기 검색창
- 등록한 링크 목록 및 관리 기능

### 주요 화면

**1. 메인 화면**

<!-- 메인 화면 스크린샷 추가 -->

**2. SHORT URL 생성 화면**

<!-- 자동 단축 URL 생성 스크린샷 추가 -->

**3. NAMING URL 생성 화면**

<!-- 이름 지정 URL 생성 스크린샷 추가 -->

**4. 이름으로 바로가기 및 링크 목록**

<!-- 이름 검색 및 링크 관리 스크린샷 추가 -->

---

## 🔄 URL 생성 및 리다이렉트 흐름

### URL 생성

```text
1. 사용자가 원본 URL 입력
             ↓
2. React에서 생성 요청
             ↓
3. Controller에서 요청 수신
             ↓
4. Service에서 URL 검증 및 정규화
             ↓
5. SecureRandom 기반 단축 코드 생성
             ↓
6. Repository에서 MySQL 저장
             ↓
7. 생성된 단축 URL 응답
             ↓
8. React에서 결과 표시
```

### URL 리다이렉트

```text
1. 사용자가 단축 URL 클릭
             ↓
2. Spring Boot에서 단축 코드 조회
             ↓
3. MySQL에서 원본 URL 검색
             ↓
4. 원본 URL 반환
             ↓
5. HTTP 리다이렉트 응답
             ↓
6. 브라우저가 원본 사이트로 이동
```

---

## 🚀 로컬 실행 방법

### 1. 프로젝트 다운로드

```bash
git clone https://github.com/kdongd/shortari.git
cd shortari
```

※ 저장소 주소 및 폴더 이름은 실제 GitHub 저장소 구성에 맞게 수정해야 합니다.

### 2. MySQL 실행

MySQL 서버를 실행하고 `shortari` 데이터베이스와 `short_url` 테이블을 준비합니다.

DB 접속 정보는 `application.properties`에서 관리하며, 비밀번호는 환경변수로 주입합니다.

```properties
spring.datasource.password=${DB_PASSWORD}
```

### 3. Spring Boot 실행

```bash
./gradlew bootRun
```

기본 백엔드 주소:

```text
http://localhost:8080
```

### 4. React 실행

```bash
cd shortari-frontend

npm install
npm run dev
```

기본 프론트엔드 주소:

```text
http://localhost:5173
```

### 5. 브라우저 접속

```text
http://localhost:5173
```

---

## 🔧 개선 및 확장 계획

### 1. 사용자 인증

현재는 로그인 없이 URL을 등록하고 관리할 수 있도록 구성했습니다.

추후 사용자 인증을 추가하여 사용자별 URL 관리 기능으로 확장할 계획입니다.

### 2. 링크 검색 고도화

현재는 React에서 조회한 목록을 기준으로 이름을 검색합니다.

데이터가 많아질 경우 백엔드 검색 API를 추가하여 서버에서 직접 검색하도록 개선할 수 있습니다.

### 3. URL 통계 기능

단축 URL별 접속 횟수 및 이용 통계를 제공하는 기능을 추가할 수 있습니다.

### 4. 배포 환경 구축

현재는 로컬 환경에서 실행하고 있습니다.

추후 서버 배포 및 도메인 연결을 통해 외부에서도 단축 URL을 사용할 수 있도록 확장할 계획입니다.

### 5. 테스트 고도화

현재 단위 테스트 및 컨트롤러 테스트를 중심으로 검증했습니다.

추후 실제 MySQL을 활용한 Repository 통합 테스트와 동시성 상황에 대한 테스트를 추가할 계획입니다.

---

## 📝 프로젝트를 통해 학습한 내용

### 1. URL 단축 서비스의 동작 원리

단축 URL은 단순히 문자열 길이를 줄이는 것이 아니라, 단축 코드와 원본 URL을 연결하여 서버가 리다이렉트를 수행하는 구조라는 점을 학습했습니다.

### 2. SecureRandom과 Base62 문자 집합

무작위 단축 코드 생성에 `SecureRandom`을 사용하고, 영문 대소문자와 숫자로 이루어진 62개 문자 집합을 활용했습니다.

또한 랜덤 코드 생성만으로 중복을 완전히 방지할 수 없으므로 데이터베이스 제약조건이 필요하다는 점을 학습했습니다.

### 3. 데이터베이스 제약조건

NOT NULL, UNIQUE, CHECK 제약조건을 적용하면서 애플리케이션의 검증 로직과 데이터베이스의 무결성 보장이 서로 다른 역할을 한다는 점을 이해했습니다.

### 4. 계층별 책임 분리

Controller, Service, Repository를 분리하여 HTTP 요청 처리, 비즈니스 로직, 데이터 접근의 책임을 구분했습니다.

이를 통해 코드의 역할을 명확히 하고 테스트하기 쉬운 구조를 구성했습니다.

### 5. 프론트엔드와 백엔드 연동

React에서 API 요청을 보내고 Spring Boot가 데이터를 처리한 뒤 JSON 응답을 반환하는 전체 흐름을 구현했습니다.

### 6. 테스트 코드 작성

JUnit 5와 Mockito를 활용하여 외부 의존성을 분리하고 비즈니스 로직을 검증했습니다.

총 46개의 테스트를 실행하여 전체 통과 결과를 확인했습니다.

---

