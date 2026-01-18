# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## Project Overview

Gotchan은 가챠(캡슐토이) 교환 서비스 백엔드입니다. Spring Boot 4.0 + Kotlin 기반으로 헥사고날 아키텍처와 DDD를 적용했습니다.

## 관련 문서

| 문서 | 설명 |
|------|------|
| `DEVELOPMENT_PLAN.md` | 개발 계획 및 Phase별 진행 상황 |
| `TDD_GUIDE.md` | TDD 개발 가이드라인 |
| `GIT_CONVENTION.md` | Git 커밋 메시지 및 브랜치 규칙 |
| `CODE_REVIEW_GUIDE.md` | AI 코드 리뷰 가이드 |

## Build Commands

```bash
# Build
./gradlew build

# Run (Docker Compose로 MySQL, Redis 자동 시작)
./gradlew bootRun

# Test
./gradlew test
./gradlew test --tests "com.gotchan.application.user.service.UserServiceTest"

# Clean
./gradlew clean build
```

## Tech Stack

- **Language**: Kotlin 2.2 + Java 21
- **Framework**: Spring Boot 4.0.1
- **Persistence**: Spring Data JPA + QueryDSL 5.1
- **Database**: MySQL (production), H2 (test)
- **Cache**: Redis
- **Build**: Gradle

## Architecture

### 헥사고날 아키텍처 (Ports & Adapters)

```
com.gotchan
├── common/                 # 공통 (config, exception, response)
├── domain/                 # 도메인 계층
│   └── {domain}/
│       ├── model/          # Entity, Enum
│       └── port/           # Repository Interface (Output Port)
├── application/            # 응용 계층
│   └── {domain}/
│       ├── dto/            # Command, Response
│       ├── port/           # UseCase Interface (Input Port)
│       └── service/        # UseCase 구현
└── adapter/
    ├── in/web/             # Controller (Input Adapter)
    │   └── {domain}/
    │       ├── {Domain}Controller.kt
    │       └── dto/        # Request DTO
    └── out/persistence/    # Repository 구현 (Output Adapter)
        └── {domain}/
            ├── {Domain}JpaRepository.kt
            ├── {Domain}RepositoryAdapter.kt
            └── {Domain}QueryRepository.kt
```

### 의존성 방향

```
Domain ← Application ← Adapter
```

- Domain은 어떤 계층에도 의존하지 않음
- Application은 Domain에만 의존
- Adapter는 Application과 Domain에 의존

## Development Rules

### 1. TDD (Test-Driven Development)

```
RED → GREEN → REFACTOR
```

1. **RED**: 실패하는 테스트 먼저 작성
2. **GREEN**: 테스트 통과하는 최소한의 코드 작성
3. **REFACTOR**: 코드 개선 (테스트 통과 유지)

### 2. 계층별 규칙

#### Domain Layer
- 비즈니스 로직은 Entity 내부에 캡슐화
- 상태 변경은 의미있는 메서드를 통해서만
- JPA 외의 인프라 의존성 금지

#### Application Layer
- Service는 UseCase 인터페이스 구현
- `@Transactional(readOnly = true)` 클래스 레벨
- `@Transactional` 쓰기 메서드에만

#### Adapter In (Controller)
- UseCase 인터페이스에만 의존
- `@Valid`로 요청 검증
- `ApiResponse`로 응답 래핑

#### Adapter Out (Repository)
- Domain Port 인터페이스 구현
- QueryDSL로 복잡한 쿼리 처리

### 3. 네이밍 컨벤션

| 유형 | 패턴 | 예시 |
|------|------|------|
| UseCase | `{Domain}UseCase` | `UserUseCase` |
| Service | `{Domain}Service` | `UserService` |
| Controller | `{Domain}Controller` | `UserController` |
| Command | `{Action}Command` | `SignUpCommand` |
| Response | `{Domain}Response` | `UserResponse` |
| Request | `{Action}Request` | `SignUpRequest` |

### 4. Git Convention

```
<type>(<scope>): <subject>

# Types: feat, fix, docs, style, refactor, test, chore, perf
# Example: feat(user): 회원가입 API 구현
```

## API Endpoints

### User API
- `POST /api/v1/users/signup` - 회원가입
- `GET /api/v1/users/{userId}/profile` - 프로필 조회
- `PATCH /api/v1/users/{userId}` - 정보 수정

### Item API
- `POST /api/v1/users/{userId}/items` - 아이템 등록
- `GET /api/v1/items/{itemId}` - 아이템 조회
- `GET /api/v1/users/{userId}/items` - 내 아이템 목록
- `GET /api/v1/items/search?seriesName=` - 시리즈 검색
- `PATCH /api/v1/users/{userId}/items/{itemId}` - 수정
- `DELETE /api/v1/users/{userId}/items/{itemId}` - 삭제

### Trade API
- `POST /api/v1/users/{userId}/trades` - 거래 요청
- `POST /api/v1/users/{userId}/trades/{tradeId}/respond` - 수락/거절
- `POST /api/v1/users/{userId}/trades/{tradeId}/tracking` - 운송장 등록
- `POST /api/v1/users/{userId}/trades/{tradeId}/confirm` - 수령 확인
- `POST /api/v1/users/{userId}/trades/{tradeId}/cancel` - 취소
- `GET /api/v1/trades/{tradeId}` - 거래 조회
- `GET /api/v1/users/{userId}/trades` - 내 거래 목록

## Code Review Checklist

코드 리뷰 시 확인 사항 (상세: `CODE_REVIEW_GUIDE.md`):

1. [ ] 계층 간 의존성 방향 위반 없음
2. [ ] 비밀번호/민감정보 평문 저장/노출 없음
3. [ ] 권한 검증 누락 없음
4. [ ] 트랜잭션 설정 올바름
5. [ ] N+1 쿼리 가능성 없음
6. [ ] 테스트 코드 존재
7. [ ] 네이밍 컨벤션 준수

## Spring Boot 4.0 주의사항

### Jackson 3.x
- `ObjectMapper`: `tools.jackson.databind.ObjectMapper`
- Annotations: `com.fasterxml.jackson.annotation.*` (변경 없음)

### Test
- `@WebMvcTest`: `org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest`
- `@MockitoBean`: `org.springframework.test.context.bean.override.mockito.MockitoBean`
- 의존성: `spring-boot-starter-webmvc-test` 필요
