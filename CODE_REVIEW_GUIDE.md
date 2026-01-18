# AI Code Review Guide

이 문서는 AI가 Gotchan 프로젝트 코드 리뷰 시 참고할 가이드입니다.

---

## 아키텍처 규칙

### 계층 구조 및 의존성 방향
```
Domain ← Application ← Adapter (In/Out)
         ↑
       Common
```

- **Domain**: 비즈니스 로직, 엔티티, Port 인터페이스
- **Application**: UseCase 구현, Service, Command/Response DTO
- **Adapter In**: Controller, Web DTO
- **Adapter Out**: Repository Adapter, JPA Repository, QueryDSL
- **Common**: 예외, 설정, 응답 포맷

### 의존성 위반 감지
```kotlin
// ❌ 위반: Domain이 Application에 의존
package com.gotchan.domain.user.model
import com.gotchan.application.user.dto.UserResponse  // 금지

// ❌ 위반: Domain이 Adapter에 의존
package com.gotchan.domain.user.model
import com.gotchan.adapter.out.persistence.UserJpaRepository  // 금지

// ❌ 위반: Application이 Adapter에 의존
package com.gotchan.application.user.service
import com.gotchan.adapter.in.web.user.dto.SignUpRequest  // 금지
```

---

## 계층별 리뷰 포인트

### 1. Domain Layer

| 항목 | 확인 사항 |
|------|----------|
| Rich Domain Model | 비즈니스 로직이 엔티티 내부에 있는가? |
| 불변성 | 변경되면 안되는 필드에 `val` 사용 |
| 상태 전이 | 의미있는 메서드로만 상태 변경 가능 |
| 검증 | 도메인 규칙은 도메인에서 검증 |

```kotlin
// ✅ 올바른 도메인 모델
class Trade {
    fun accept() {
        validateStatusTransition(TradeStatus.PENDING, TradeStatus.ACCEPTED)
        status = TradeStatus.ACCEPTED
        proposerItem.startTrading()
    }
}

// ❌ 빈약한 도메인 모델
class Trade {
    var status: TradeStatus = TradeStatus.PENDING  // setter로 직접 변경 가능
}
```

### 2. Application Layer

| 항목 | 확인 사항 |
|------|----------|
| UseCase 구현 | Service가 UseCase 인터페이스 구현 |
| 트랜잭션 | 클래스: `@Transactional(readOnly = true)`, 쓰기 메서드: `@Transactional` |
| 예외 처리 | `BusinessException` 계열 사용 |
| DTO 변환 | Response에 `from()` 팩토리 메서드 |

```kotlin
// ✅ 올바른 트랜잭션 설정
@Service
@Transactional(readOnly = true)
class UserService : UserUseCase {
    @Transactional
    override fun signUp(command: SignUpCommand): UserResponse { }

    override fun getProfile(userId: UUID): UserProfileResponse { }  // readOnly
}
```

### 3. Adapter In (Controller)

| 항목 | 확인 사항 |
|------|----------|
| 의존성 | UseCase 인터페이스에만 의존 |
| 검증 | `@Valid` 어노테이션 적용 |
| 응답 | `ApiResponse`로 래핑, 적절한 HTTP 상태 코드 |
| 로직 | 비즈니스 로직 없음 (위임만) |

```kotlin
// ✅ 올바른 Controller
@PostMapping("/signup")
fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<Any>> {
    val response = userUseCase.signUp(request.toCommand())
    return ResponseEntity.status(HttpStatus.CREATED).body(ApiResponse.success(response))
}
```

### 4. Adapter Out (Repository)

| 항목 | 확인 사항 |
|------|----------|
| Port 구현 | Domain Port 인터페이스 구현 |
| QueryDSL | Q클래스 필드 캐싱, `fetchFirst() != null` 패턴 |
| N+1 방지 | 연관 엔티티 조회 시 Fetch Join 고려 |

---

## 보안 체크 (Critical)

반드시 확인해야 할 보안 항목:

| 항목 | 확인 방법 |
|------|----------|
| 비밀번호 암호화 | `password = command.password` 형태 금지 |
| 권한 검증 | 리소스 접근 전 소유자/권한 확인 |
| 민감정보 노출 | Response에 password, token 등 포함 금지 |
| SQL Injection | Native Query 사용 시 파라미터 바인딩 확인 |

```kotlin
// ❌ 권한 검증 누락
fun deleteItem(itemId: Long) {
    itemRepository.deleteById(itemId)  // 누구나 삭제 가능
}

// ✅ 권한 검증 포함
fun deleteItem(command: DeleteItemCommand) {
    val item = itemRepository.findById(command.itemId)
    if (item.owner.id != command.requesterId) {
        throw ForbiddenException("Not the owner")
    }
    itemRepository.delete(item)
}
```

---

## 성능 체크

### N+1 쿼리 감지
```kotlin
// ⚠️ N+1 가능성
fun getTradesByUser(userId: UUID): List<TradeResponse> {
    return tradeRepository.findByUserId(userId)
        .map { TradeResponse.from(it) }  // trade.proposer, trade.receiver 접근 시 추가 쿼리
}
```

### 해결 방안
- `@EntityGraph(attributePaths = [...])`
- QueryDSL `fetchJoin()`
- JPQL `JOIN FETCH`

---

## 코드 스타일

### Kotlin 컨벤션
- 함수명: camelCase, 동사로 시작
- 클래스명: PascalCase
- 상수: UPPER_SNAKE_CASE
- 한 줄 함수: `fun isAvailable() = status == ItemStatus.AVAILABLE`

### 네이밍 규칙
| 유형 | 패턴 | 예시 |
|------|------|------|
| UseCase | `{Domain}UseCase` | `UserUseCase` |
| Service | `{Domain}Service` | `UserService` |
| Controller | `{Domain}Controller` | `UserController` |
| Repository Port | `{Domain}Repository` | `UserRepository` |
| Repository Adapter | `{Domain}RepositoryAdapter` | `UserRepositoryAdapter` |
| Command DTO | `{Action}{Domain}Command` | `SignUpCommand` |
| Response DTO | `{Domain}Response` | `UserResponse` |
| Request DTO | `{Action}Request` | `SignUpRequest` |

---

## 리뷰 결과 형식

코드 리뷰 시 다음 형식으로 결과 제시:

```markdown
## 코드 리뷰 결과

### 🔴 Critical (반드시 수정)
- [파일:라인] 문제 설명
  - 현재: `문제 코드`
  - 권장: `수정 코드`

### 🟡 Major (수정 권장)
- [파일:라인] 문제 설명

### 🟢 Minor (개선 제안)
- [파일:라인] 개선 제안

### ✅ 잘된 점
- 칭찬할 부분
```

---

## 빠른 체크리스트

리뷰 시 순서대로 확인:

1. [ ] 계층 간 의존성 방향 위반 없음
2. [ ] 비밀번호/민감정보 평문 저장/노출 없음
3. [ ] 권한 검증 누락 없음
4. [ ] 트랜잭션 설정 올바름
5. [ ] N+1 쿼리 가능성 없음
6. [ ] 테스트 코드 존재
7. [ ] 네이밍 컨벤션 준수
