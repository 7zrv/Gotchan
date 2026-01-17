# Gacha Exchange Service - 개발 계획서

## 프로젝트 정보

- **프로젝트명**: Gotchan (Gacha-Swap)
- **기술 스택**: Spring Boot 4.0, Kotlin, JPA, MySQL, Redis
- **Java 버전**: 21

---

## Phase 1: 기반 구조 구축

### 1.1 프로젝트 설정
- [ ] 패키지 구조 설계 (domain, application, infrastructure, presentation)
- [ ] 공통 응답 포맷 정의 (ApiResponse, ErrorResponse)
- [ ] 예외 처리 핸들러 구현 (GlobalExceptionHandler)
- [ ] API 버전 관리 설정 (/api/v1)

### 1.2 보안 설정
- [ ] Spring Security 의존성 추가
- [ ] JWT 인증 구현
- [ ] 비밀번호 암호화 (BCrypt)
- [ ] 배송지 정보 암호화 모듈 구현

### 1.3 인프라 설정
- [ ] AWS S3 연동 설정 (이미지 업로드)
- [ ] Redis 캐시 설정
- [ ] Docker Compose 환경 보완

---

## Phase 2: 도메인 모델 구현

### 2.1 User 엔티티
```kotlin
// 주요 필드
- id: UUID (PK)
- nickname: String
- email: String
- password: String (암호화)
- trustScore: BigDecimal (기본값 36.5)
- addressHash: String (암호화)
- createdAt, updatedAt
```

### 2.2 GachaItem 엔티티
```kotlin
// 주요 필드
- id: Long (PK)
- owner: User (FK)
- seriesName: String
- itemName: String
- imageUrl: String
- status: ItemStatus (AVAILABLE, TRADING, COMPLETED)
- type: ItemType (HAVE, WISH)
- createdAt, updatedAt
```

### 2.3 Trade 엔티티
```kotlin
// 주요 필드
- id: Long (PK)
- proposer: User (FK)
- receiver: User (FK)
- proposerItem: GachaItem (FK)
- receiverItem: GachaItem (FK)
- status: TradeStatus (PENDING, ACCEPTED, SHIPPING, FINISHED, CANCELLED)
- trackingNumber: String?
- createdAt, updatedAt
```

---

## Phase 3: API 개발

### 3.1 인증/유저 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/auth/signup` | 회원가입 |
| POST | `/api/v1/auth/login` | 로그인 (JWT 발급) |
| GET | `/api/v1/users/{userId}/profile` | 프로필 조회 |
| PATCH | `/api/v1/users/me` | 내 정보 수정 |

### 3.2 물품 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/items` | 아이템 등록 |
| GET | `/api/v1/items` | 아이템 목록 (필터링) |
| GET | `/api/v1/items/match` | 스마트 매칭 조회 |
| GET | `/api/v1/items/{itemId}` | 아이템 상세 |
| PATCH | `/api/v1/items/{itemId}` | 아이템 수정 |
| DELETE | `/api/v1/items/{itemId}` | 아이템 삭제 |

### 3.3 교환 API
| Method | Endpoint | 설명 |
|--------|----------|------|
| POST | `/api/v1/trades/request` | 교환 제안 |
| GET | `/api/v1/trades` | 내 교환 목록 |
| GET | `/api/v1/trades/{tradeId}` | 교환 상세 |
| PATCH | `/api/v1/trades/{tradeId}/status` | 상태 변경 |
| POST | `/api/v1/trades/{tradeId}/tracking` | 운송장 등록 |

---

## Phase 4: 핵심 비즈니스 로직

### 4.1 스마트 매칭 알고리즘
- [ ] 내 WISH와 상대 HAVE 매칭
- [ ] 내 HAVE와 상대 WISH 매칭
- [ ] 양방향 매칭 결과 조합
- [ ] Redis 캐싱으로 성능 최적화

### 4.2 교환 상태 머신 (State Machine)
```
PENDING → ACCEPTED → SHIPPING → FINISHED
    ↓         ↓          ↓
 CANCELLED  CANCELLED  CANCELLED
```
- [ ] 상태 전이 규칙 검증 로직
- [ ] 양측 확인 완료 시에만 FINISHED 처리

### 4.3 신뢰도 시스템
- [ ] 거래 완료 시 trustScore 증가
- [ ] 거래 취소/신고 시 trustScore 감소
- [ ] 신고 처리 로직 구현

---

## Phase 5: 부가 기능

### 5.1 이미지 처리
- [ ] S3 업로드 서비스
- [ ] 이미지 워터마킹 (유저 ID + 날짜)
- [ ] 썸네일 생성

### 5.2 알림 시스템
- [ ] Redis Pub/Sub 기반 실시간 알림
- [ ] 매칭 발생 시 알림
- [ ] 교환 상태 변경 시 알림

### 5.3 검색/필터링
- [ ] 시리즈별 검색
- [ ] 캐릭터/아이템명 검색
- [ ] 정렬 (최신순, 신뢰도순)

---

## Phase 6: 테스트 및 배포

### 6.1 테스트
- [ ] 단위 테스트 (Service, Repository)
- [ ] 통합 테스트 (API 엔드포인트)
- [ ] 매칭 알고리즘 테스트

### 6.2 문서화
- [ ] Swagger/OpenAPI 문서 설정
- [ ] API 문서 작성

### 6.3 배포
- [ ] 프로덕션 환경 설정
- [ ] CI/CD 파이프라인 구축

---

## 패키지 구조 (제안)

```
com.gotchan
├── common
│   ├── config          # 설정 클래스
│   ├── exception       # 예외 처리
│   ├── response        # 공통 응답
│   └── security        # 보안 설정
├── domain
│   ├── user
│   │   ├── entity
│   │   ├── repository
│   │   ├── service
│   │   └── controller
│   ├── item
│   │   ├── entity
│   │   ├── repository
│   │   ├── service
│   │   └── controller
│   └── trade
│       ├── entity
│       ├── repository
│       ├── service
│       └── controller
└── infrastructure
    ├── s3              # S3 클라이언트
    └── redis           # Redis 설정
```

---

## 우선순위 요약

| 순위 | 항목 | 비고 |
|------|------|------|
| 1 | Phase 1, 2 | 기반 구조 + 도메인 모델 |
| 2 | Phase 3.1, 3.2 | 유저/아이템 API |
| 3 | Phase 4.1 | 스마트 매칭 (핵심 기능) |
| 4 | Phase 3.3, 4.2 | 교환 API + 상태 관리 |
| 5 | Phase 5, 6 | 부가 기능 + 배포 |
