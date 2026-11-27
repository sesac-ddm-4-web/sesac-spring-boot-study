# Study Management Service

Spring Boot와 Spring Data JPA를 기반으로 구현한 스터디 관리 서비스입니다.

회원 및 프로필 관리, 스터디 생성, 참여 및 탈퇴, 일정 관리 기능을 제공하며, JPA의 엔티티 연관관계와 트랜잭션을 활용하여 데이터 정합성을 관리합니다.

## 1. 프로젝트 개요

### 개발 목적

- 스터디 생성 및 참여 관리 서비스 구현
- Spring Data JPA를 활용한 데이터 영속성 관리
- 엔티티 간 연관관계 설계 및 매핑
- 동시성 제어를 통한 스터디 정원 초과 방지

## 2. 기술 스택

- Java
- Spring Boot
- Spring Data JPA
- Hibernate
- MySQL
- Gradle
- Jakarta Validation

## 3. 주요 기능

### 회원 관리

- 회원 등록 및 상세 조회
- 회원 이름 및 프로필 수정
- 회원과 프로필 간 일대일 연관관계 구성

### 스터디 관리

- 스터디 생성 및 전체 목록 조회
- 스터디 상세 조회
- 스터디장 지정 및 자동 참여 처리
- 스터디별 최대 참여 인원 관리

### 참여 관리

- 스터디 참여 및 탈퇴
- 스터디 참여자 목록 조회
- 중복 참여 및 정원 초과 방지
- 스터디장 탈퇴 제한
- 비관적 락을 활용한 참여 및 탈퇴 동시성 제어

### 일정 관리

- 스터디 일정 등록 및 조회
- 스터디장만 일정 등록 가능
- 과거 일정 등록 제한
- 시작 시간 기준 일정 정렬

## 4. 프로젝트 구조

```
study-manage/
├── src/
│   ├── main/
│   │   ├── java/
│   │   │   └── com.example.sesac_spring_boot_study/
│   │   │       ├── controller/
│   │   │       │   └── StudyManagementController.java
│   │   │       ├── domain/
│   │   │       │   ├── Meeting.java
│   │   │       │   ├── Member.java
│   │   │       │   ├── Participation.java
│   │   │       │   ├── Profile.java
│   │   │       │   └── StudyGroup.java
│   │   │       ├── repository/
│   │   │       │   ├── MeetingRepository.java
│   │   │       │   ├── MemberRepository.java
│   │   │       │   ├── ParticipationRepository.java
│   │   │       │   ├── ProfileRepository.java
│   │   │       │   └── StudyGroupRepository.java
│   │   │       ├── service/
│   │   │       │   └── StudyManagementService.java
│   │   │       └── SesacSpringBootStudyApplication.java
│   │   └── resources/
│   └── test/
├── gradle/
├── build.gradle
├── settings.gradle
├── gradlew
├── gradlew.bat
└── README.md
```

## 5. 도메인 구성

| 엔티티 | 테이블 | 역할 |
|---|---|---|
| Member | members | 회원 정보 관리 |
| Profile | member_profiles | 회원 프로필 관리 |
| StudyGroup | study_groups | 스터디 기본 정보 및 정원 관리 |
| Participation | study_participations | 회원의 스터디 참여 정보 관리 |
| Meeting | study_meetings | 스터디 일정 관리 |

### 엔티티 연관관계

- Member ↔ Profile: 1:1
- Member ↔ StudyGroup: 1:N (스터디장)
- Member ↔ Participation: 1:N
- StudyGroup ↔ Participation: 1:N
- StudyGroup ↔ Meeting: 1:N

회원과 스터디 간 N:M 관계는 Participation 엔티티를 통해 관리합니다.

## 6. API 명세

| Method | URL | 기능 |
|---|---|---|
| POST | /api/members | 회원 등록 |
| GET | /api/members/{memberId} | 회원 조회 |
| PUT | /api/members/{memberId} | 회원 정보 수정 |
| POST | /api/studies | 스터디 생성 |
| GET | /api/studies | 스터디 목록 조회 |
| GET | /api/studies/{studyId} | 스터디 상세 조회 |
| POST | /api/studies/{studyId}/participants | 스터디 참여 |
| DELETE | /api/studies/{studyId}/participants/{memberId} | 스터디 탈퇴 |
| GET | /api/studies/{studyId}/participants | 참여자 목록 조회 |
| POST | /api/studies/{studyId}/meetings | 일정 등록 |
| GET | /api/studies/{studyId}/meetings | 일정 목록 조회 |

## 7. 주요 기술적 고려사항

### 1. Spring Data JPA 기반 데이터 접근

- JpaRepository를 활용한 기본 CRUD 구현
- 메서드 이름 기반 쿼리 및 JPQL 활용
- 영속성 컨텍스트와 변경 감지를 활용한 회원 정보 수정

### 2. 데이터 정합성 관리

- @Transactional을 활용한 트랜잭션 관리
- 회원 ID와 스터디 ID에 복합 유니크 제약조건 적용
- 회원과 프로필 간 일대일 관계 보장

### 3. 동시성 제어

- PESSIMISTIC_WRITE를 활용한 스터디 행 잠금
- 동일한 스터디의 참여 및 탈퇴 요청 직렬화
- 동시 참여 요청에 따른 정원 초과 방지

### 4. 조회 성능 고려

- FetchType.LAZY를 활용한 지연 로딩
- @EntityGraph를 활용한 참여자 조회 시 회원 정보 동시 로딩
- 읽기 전용 트랜잭션과 쓰기 트랜잭션 구분

### 5. 요청 검증 및 예외 처리

- Jakarta Validation을 활용한 요청 데이터 검증
- 서비스 계층에서 비즈니스 규칙 검증
- HTTP 상태 코드를 활용한 예외 응답 처리