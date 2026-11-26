# KingDom - Shorten URL Service

Spring Boot 기반의 **단축 URL 생성 및 관리 서비스**로, 왕·신하·역적이 참여하는 역할 기반 추리 게임 **KingDom**을 운영하기 위해 개발한 프로젝트입니다.

참가자별 고유한 단축 URL을 발급하고, 게임 진행에 따라 연결된 노션 페이지를 변경하거나 링크를 비활성화할 수 있습니다.

## 1. 프로젝트 개요

KingDom은 **왕 1명, 신하 7명, 역적 2명**으로 구성된 10인 추리 게임입니다.

각 역할은 서로 다른 목표를 가지고 게임에 참여합니다.

- **왕:** 사흘 동안 제공되는 단서를 활용해 숨겨진 보물 찾기
- **신하:** 단서를 바탕으로 역적 2명의 정체 추리
- **역적:** 자신의 정체를 숨기면서 왕의 정체 추리

게임 시작 시 참가자는 고유한 단축 URL을 통해 자신의 역할 소개 페이지를 확인합니다. 이후 Day 1~3에 걸쳐 공개되는 힌트를 동일한 URL로 확인할 수 있습니다.

이를 위해 **단축 코드는 유지하면서 원본 URL만 변경하는 기능**을 구현했습니다.

## 2. 주요 기능

| 기능 | 설명 |
|---|---|
| 단축 URL 생성 | 원본 URL을 8자리 고유 단축 코드로 변환 |
| URL 리다이렉트 | 단축 URL 접속 시 연결된 노션 페이지로 이동 |
| 참가자 관리 | 참가자 이름 및 역할(왕·신하·역적) 지정 |
| URL 목록 조회 | 검색 및 페이지네이션을 통한 링크 관리 |
| URL 봉인·재개 | 참가자별 링크 비활성화 및 재활성화 |
| 원본 URL 변경 | 단축 코드를 유지하면서 연결된 노션 페이지 변경 |
| 힌트 관리 | 역할별 Day 1~3 노션 URL 등록 및 확인 |

## 3. 게임 운영 흐름

1. 관리자가 참가자 10명에게 각각 고유한 단축 URL을 발급합니다.
2. 참가자는 자신의 URL로 접속해 역할 소개 페이지를 확인합니다.
3. Day 1이 시작되면 관리자가 해당 단축 URL의 목적지를 첫 번째 힌트 페이지로 변경합니다.
4. Day 2~3에도 동일한 방식으로 새로운 힌트를 공개합니다.
5. 게임 진행 중 탈락한 참가자의 단축 URL은 봉인할 수 있습니다.

참가자는 새로운 링크를 발급받을 필요 없이 **처음 받은 단축 URL 하나로 게임에 참여**할 수 있습니다.

## 4. 기술 스택

| 구분 | 기술 |
|---|---|
| Backend | Java, Spring Boot |
| Frontend | HTML, CSS, JavaScript |
| Database | MySQL |
| Data Access | JPA (Hibernate) |
| Infrastructure | Docker |
| Build Tool | Gradle |
| Architecture | Layered Architecture |

단축 URL 저장소를 `JdbcTemplate`에서 JPA로 전환했습니다. `ShortenUrl` 엔티티는 기존 MySQL `shorten_urls` 테이블에 매핑되며, 저장소는 `EntityManager`와 JPQL을 사용합니다.

## 5. 프로젝트 구조

```text
src/
└── main/
    ├── java/
    │   └── kr/co/shortenurlservice/
    │       ├── application/
    │       │   └── SimpleShortenUrlService.java
    │       ├── domain/
    │       │   ├── ShortenUrl.java
    │       │   └── ShortenUrlRepository.java
    │       ├── infrastructure/
    │       │   └── JpaShortenUrlRepository.java
    │       ├── presentation/
    │       │   ├── ShortenUrlCreateRequestDto.java
    │       │   ├── ShortenUrlCreateResponseDto.java
    │       │   ├── ShortenUrlInformationDto.java
    │       │   ├── ShortenUrlListResponseDto.java
    │       │   ├── ShortenUrlRestController.java
    │       │   ├── ShortenUrlStatusRequestDto.java
    │       │   └── ShortenUrlUpdateRequestDto.java
    │       └── ShortenurlserviceApplication.java
    └── resources/
        ├── static/
        │   └── index.html
        └── application.properties
```

## 6. 시스템 동작 구조

```text
관리자 / 참가자
       │
       ▼
Frontend (HTML, CSS, JavaScript)
       │
       ▼
Controller (REST API)
       │
       ▼
Service (비즈니스 로직)
       │
       ▼
Repository (JPA)
       │
       ▼
MySQL (Docker)
```

Spring Boot의 계층형 아키텍처를 적용하여 요청 처리, 비즈니스 로직, 데이터 접근 역할을 분리했습니다.

- **Controller:** 단축 URL 생성·조회·변경 요청 처리
- **Service:** 단축 코드 생성, 활성 상태 확인, 목적지 변경 등 비즈니스 로직 수행
- **Repository:** JPA를 이용한 단축 URL 저장·조회·수정
- **Frontend:** 참가자 정보 및 단축 URL을 관리하는 웹 화면 제공

## 7. 데이터 저장 방식

| 데이터 | 저장 위치 |
|---|---|
| 단축 코드 및 원본 URL | MySQL |
| URL 활성화·봉인 상태 | MySQL |
| URL 접속 횟수 | MySQL |
| 참가자 이름 및 역할 | 브라우저 localStorage |
| Day 1~3 힌트 URL | 브라우저 sessionStorage |

단축 URL의 핵심 정보는 MySQL에 저장하며, 게임 진행을 위한 참가자 정보와 힌트 목록은 브라우저 저장소를 활용합니다.

## 8. 프로젝트 특징

- **독립적인 URL 관리:** 같은 역할의 참가자라도 서로 다른 단축 URL을 발급받아 개별적으로 관리할 수 있습니다.
- **단축 URL 재사용:** 단축 코드를 변경하지 않고 원본 URL만 수정하여 게임 진행 상황에 맞는 힌트를 제공합니다.
- **링크 접근 제어:** 봉인된 단축 URL은 리다이렉트를 차단하여 탈락자의 힌트 접근을 제한할 수 있습니다.
- **계층형 구조:** Controller, Service, Repository의 책임을 분리하여 유지보수와 기능 확장이 용이하도록 구성했습니다.
