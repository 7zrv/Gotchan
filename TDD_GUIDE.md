# TDD Development Guide for Gotchan

이 문서는 Gotchan 프로젝트의 TDD(Test-Driven Development) 개발 가이드입니다.

## TDD 기본 사이클

모든 기능 개발은 반드시 다음 사이클을 따릅니다:

```
1. RED    → 실패하는 테스트 작성
2. GREEN  → 테스트를 통과하는 최소한의 코드 작성
3. REFACTOR → 코드 개선 (테스트는 계속 통과해야 함)
```

## 개발 순서

기능 개발 시 다음 순서를 준수합니다:

### 1. Domain Layer (도메인 레이어)
```
테스트: src/test/kotlin/com/gotchan/domain/{도메인}/model/*Test.kt
구현: src/main/kotlin/com/gotchan/domain/{도메인}/model/*.kt
```

### 2. Application Layer (애플리케이션 레이어)
```
테스트: src/test/kotlin/com/gotchan/application/{도메인}/service/*ServiceTest.kt
구현: src/main/kotlin/com/gotchan/application/{도메인}/service/*.kt
```

### 3. Adapter Layer (어댑터 레이어)
```
테스트: src/test/kotlin/com/gotchan/adapter/out/persistence/{도메인}/*RepositoryTest.kt
테스트: src/test/kotlin/com/gotchan/adapter/in/web/{도메인}/*ControllerTest.kt
구현: src/main/kotlin/com/gotchan/adapter/**/*.kt
```

## 테스트 작성 규칙

### 테스트 클래스 네이밍
```kotlin
// 단위 테스트
{클래스명}Test.kt

// 통합 테스트
{클래스명}IntegrationTest.kt
```

### 테스트 메서드 네이밍 (한글 사용 권장)
```kotlin
@Test
fun `아이템이 AVAILABLE 상태일 때 거래를 시작할 수 있다`() { }

@Test
fun `이미 거래 중인 아이템은 다시 거래를 시작할 수 없다`() { }
```

### Given-When-Then 패턴
```kotlin
@Test
fun `사용자 신뢰도는 0 미만으로 내려가지 않는다`() {
    // Given: 초기 상태 설정
    val user = User(
        nickname = "tester",
        email = "test@test.com",
        password = "password"
    )

    // When: 행위 실행
    user.decreaseTrustScore(BigDecimal("100"))

    // Then: 결과 검증
    assertThat(user.trustScore).isEqualTo(BigDecimal.ZERO)
}
```

## 레이어별 테스트 가이드

### Domain 테스트 (단위 테스트)
- 외부 의존성 없이 순수 도메인 로직만 테스트
- Mocking 최소화, 실제 객체 사용
- 비즈니스 규칙 검증에 집중

```kotlin
class GachaItemTest {

    @Test
    fun `AVAILABLE 상태의 아이템만 거래를 시작할 수 있다`() {
        // Given
        val item = createAvailableItem()

        // When
        item.startTrading()

        // Then
        assertThat(item.status).isEqualTo(ItemStatus.TRADING)
    }

    @Test
    fun `TRADING 상태의 아이템은 거래를 시작할 수 없다`() {
        // Given
        val item = createTradingItem()

        // When & Then
        assertThrows<InvalidStateException> {
            item.startTrading()
        }
    }
}
```

### Application Service 테스트 (단위 테스트)
- Repository는 Mock 사용
- UseCase/비즈니스 흐름 검증
- 예외 상황 테스트 포함

```kotlin
@ExtendWith(MockitoExtension::class)
class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    @InjectMocks
    lateinit var userService: UserService

    @Test
    fun `이메일이 중복되면 회원가입에 실패한다`() {
        // Given
        val command = SignUpCommand(
            email = "existing@test.com",
            nickname = "newuser",
            password = "password"
        )
        given(userRepository.existsByEmail(command.email)).willReturn(true)

        // When & Then
        assertThrows<DuplicateEntityException> {
            userService.signUp(command)
        }
    }
}
```

### Repository 테스트 (통합 테스트)
- @DataJpaTest 사용
- 실제 DB 쿼리 검증 (H2)
- 데이터 정합성 확인

```kotlin
@DataJpaTest
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
@ActiveProfiles("test")
class ItemRepositoryTest {

    @Autowired
    lateinit var itemRepository: ItemRepository

    @Test
    fun `시리즈명으로 아이템을 조회할 수 있다`() {
        // Given
        val item = createAndSaveItem(seriesName = "원피스")

        // When
        val result = itemRepository.findBySeriesName("원피스")

        // Then
        assertThat(result).hasSize(1)
        assertThat(result[0].seriesName).isEqualTo("원피스")
    }
}
```

### Controller 테스트 (통합 테스트)
- @WebMvcTest 또는 @SpringBootTest 사용
- HTTP 요청/응답 검증
- 인증/인가 테스트 포함

```kotlin
@WebMvcTest(ItemController::class)
class ItemControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @MockBean
    lateinit var itemService: ItemService

    @Test
    fun `아이템 등록 성공 시 201을 반환한다`() {
        // Given
        val request = CreateItemRequest(
            seriesName = "원피스",
            itemName = "루피",
            type = ItemType.HAVE
        )

        // When & Then
        mockMvc.perform(
            post("/api/v1/items")
                .contentType(MediaType.APPLICATION_JSON)
                .content(objectMapper.writeValueAsString(request))
        )
        .andExpect(status().isCreated)
        .andExpect(jsonPath("$.success").value(true))
    }
}
```

## 테스트 실행 명령어

```bash
# 전체 테스트
./gradlew test

# 특정 테스트 클래스
./gradlew test --tests "com.gotchan.domain.item.model.GachaItemTest"

# 특정 테스트 메서드
./gradlew test --tests "com.gotchan.domain.item.model.GachaItemTest.AVAILABLE 상태의 아이템만 거래를 시작할 수 있다"

# 특정 패키지
./gradlew test --tests "com.gotchan.domain.*"

# 테스트 리포트 확인
open build/reports/tests/test/index.html
```

## TDD 체크리스트

새 기능 개발 시 다음을 확인합니다:

- [ ] 테스트 먼저 작성했는가?
- [ ] 테스트가 실패하는 것을 확인했는가? (RED)
- [ ] 테스트를 통과하는 최소한의 코드를 작성했는가? (GREEN)
- [ ] 코드를 리팩토링했는가? (REFACTOR)
- [ ] 모든 테스트가 통과하는가?
- [ ] 엣지 케이스를 테스트했는가?
- [ ] 예외 상황을 테스트했는가?

## 테스트 커버리지 목표

| 레이어 | 목표 커버리지 |
|--------|---------------|
| Domain Model | 90% 이상 |
| Application Service | 80% 이상 |
| Controller | 70% 이상 |

## 테스트 픽스처

반복되는 테스트 데이터는 픽스처로 관리합니다:

```kotlin
// src/test/kotlin/com/gotchan/fixture/UserFixture.kt
object UserFixture {
    fun createUser(
        nickname: String = "tester",
        email: String = "test@test.com",
        password: String = "password123"
    ) = User(
        nickname = nickname,
        email = email,
        password = password
    )
}

// src/test/kotlin/com/gotchan/fixture/ItemFixture.kt
object ItemFixture {
    fun createHaveItem(
        owner: User,
        seriesName: String = "원피스",
        itemName: String = "루피"
    ) = GachaItem(
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        type = ItemType.HAVE
    )
}
```

## AI 개발 지시사항

AI가 기능을 구현할 때 다음 순서를 따릅니다:

1. **요구사항 분석** → 구현할 기능 명확히 정의
2. **테스트 작성** → 실패하는 테스트 먼저 작성
3. **테스트 실행** → RED 상태 확인
4. **구현** → 테스트를 통과하는 최소 코드 작성
5. **테스트 실행** → GREEN 상태 확인
6. **리팩토링** → 코드 개선
7. **테스트 실행** → 여전히 GREEN 확인
8. **다음 테스트** → 사이클 반복
