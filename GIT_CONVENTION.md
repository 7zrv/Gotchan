# Git Convention Guide for Gotchan

이 문서는 Gotchan 프로젝트의 Git 사용 규칙을 정의합니다.

## 커밋 메시지 형식

```
<type>(<scope>): <subject>

<body>

<footer>
```

### Type (필수)

| Type | 설명 |
|------|------|
| `feat` | 새로운 기능 추가 |
| `fix` | 버그 수정 |
| `docs` | 문서 수정 (README, CLAUDE.md 등) |
| `style` | 코드 포맷팅, 세미콜론 누락 등 (로직 변경 없음) |
| `refactor` | 코드 리팩토링 (기능 변경 없음) |
| `test` | 테스트 코드 추가/수정 |
| `chore` | 빌드 설정, 패키지 매니저 설정 등 |
| `perf` | 성능 개선 |

### Scope (선택)

영향 받는 범위를 명시합니다:
- `user` - 사용자 도메인
- `item` - 아이템 도메인
- `trade` - 교환 도메인
- `common` - 공통 모듈
- `config` - 설정
- `deps` - 의존성

### Subject (필수)

- 50자 이내
- 마침표 없이 작성
- 명령문으로 작성 (한글: "~추가", "~수정", "~삭제")

### Body (선택)

- 72자 단위로 줄바꿈
- "무엇을", "왜" 변경했는지 설명
- How보다 What/Why 중심

### Footer (선택)

- Breaking Changes: `BREAKING CHANGE:` 접두사
- 이슈 참조: `Closes #123`, `Fixes #456`

## 커밋 메시지 예시

### 기능 추가
```
feat(user): 회원가입 서비스 구현

- SignUpCommand, UserResponse DTO 추가
- UserService.signUp() 메서드 구현
- 이메일/닉네임 중복 검증 로직 추가
```

### 테스트 추가
```
test(user): UserService 단위 테스트 추가

- 회원가입 성공/실패 케이스 테스트
- 프로필 조회 테스트
- 정보 수정 테스트
```

### 버그 수정
```
fix(trade): 교환 상태 전이 오류 수정

TRADING 상태가 TradeStatus enum에 없어서 발생한
컴파일 오류 수정

Fixes #42
```

### 문서 수정
```
docs: TDD 개발 가이드 문서 추가

- TDD_GUIDE.md 생성
- 레이어별 테스트 작성 가이드
- 테스트 픽스처 사용법
```

### 리팩토링
```
refactor(common): 예외 처리 구조 개선

- ErrorCode enum 분리
- BusinessException 계층 구조화
```

## 브랜치 전략

### 브랜치 네이밍

```
<type>/<issue-number>-<short-description>
```

예시:
- `feat/12-user-signup`
- `fix/45-trade-status-bug`
- `refactor/30-exception-handling`

### 주요 브랜치

| 브랜치 | 용도 |
|--------|------|
| `main` | 프로덕션 배포 브랜치 |
| `develop` | 개발 통합 브랜치 |
| `feat/*` | 기능 개발 |
| `fix/*` | 버그 수정 |
| `refactor/*` | 리팩토링 |

## PR (Pull Request) 규칙

### PR 제목
```
[<type>] <subject>
```

예시: `[feat] 회원가입 API 구현`

### PR 본문 템플릿
```markdown
## 변경 사항
-

## 테스트
- [ ] 단위 테스트 통과
- [ ] 통합 테스트 통과

## 관련 이슈
Closes #
```

## AI 개발 시 Git 규칙

1. **커밋 단위**: 논리적으로 완결된 작업 단위로 커밋
2. **TDD 커밋 순서**:
   - `test(scope): 실패하는 테스트 작성` (RED)
   - `feat(scope): 테스트 통과하는 구현` (GREEN)
   - `refactor(scope): 코드 개선` (REFACTOR) - 필요시
3. **커밋 전 확인**:
   - `./gradlew build` 성공 확인
   - 테스트 통과 확인
4. **민감 정보 제외**: `.env`, 비밀번호, API 키 등 커밋 금지
