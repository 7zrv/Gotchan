package com.gotchan.application.item.service

import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.user.port.UserRepository
import com.gotchan.fixture.UserFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.given
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("MatchingService")
class MatchingServiceTest {

    @Mock
    lateinit var itemRepository: ItemRepository

    @Mock
    lateinit var userRepository: UserRepository

    lateinit var matchingService: MatchingService

    @BeforeEach
    fun setUp() {
        matchingService = MatchingService(itemRepository, userRepository)
    }

    @Nested
    @DisplayName("스마트 매칭")
    inner class FindMatchesTest {

        @Test
        fun `양방향 매칭이 되는 상대를 찾을 수 있다`() {
            // Given
            // User A: 루피(HAVE), 조로(WISH)
            // User B: 조로(HAVE), 루피(WISH)
            val userA = UserFixture.createUser(nickname = "userA", email = "a@test.com")
            val userB = UserFixture.createUser(nickname = "userB", email = "b@test.com")

            val userAHave = createItem(1L, userA, "원피스", "루피", ItemType.HAVE)
            val userAWish = createItem(2L, userA, "원피스", "조로", ItemType.WISH)
            val userBHave = createItem(3L, userB, "원피스", "조로", ItemType.HAVE)
            val userBWish = createItem(4L, userB, "원피스", "루피", ItemType.WISH)

            given(userRepository.findById(userA.id)).willReturn(userA)
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.HAVE))
                .willReturn(listOf(userAHave))
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.WISH))
                .willReturn(listOf(userAWish))

            // 내 HAVE(루피)를 WISH하는 상대의 아이템 조회
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.WISH, "원피스", "루피"
            )).willReturn(listOf(userBWish))

            // 내 WISH(조로)를 HAVE하는 상대의 아이템 조회
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.HAVE, "원피스", "조로"
            )).willReturn(listOf(userBHave))

            // When
            val result = matchingService.findMatches(userA.id)

            // Then
            assertThat(result).hasSize(1)
            assertThat(result[0].partnerId).isEqualTo(userB.id)
            assertThat(result[0].myHaveItem.itemName).isEqualTo("루피")
            assertThat(result[0].partnerHaveItem.itemName).isEqualTo("조로")
        }

        @Test
        fun `매칭되는 상대가 없으면 빈 리스트를 반환한다`() {
            // Given
            val user = UserFixture.createUser()
            val userHave = createItem(1L, user, "원피스", "루피", ItemType.HAVE)
            val userWish = createItem(2L, user, "원피스", "조로", ItemType.WISH)

            given(userRepository.findById(user.id)).willReturn(user)
            given(itemRepository.findByOwnerIdAndType(user.id, ItemType.HAVE))
                .willReturn(listOf(userHave))
            given(itemRepository.findByOwnerIdAndType(user.id, ItemType.WISH))
                .willReturn(listOf(userWish))
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.WISH, "원피스", "루피"
            )).willReturn(emptyList())

            // When
            val result = matchingService.findMatches(user.id)

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        fun `동일 시리즈 내에서만 매칭된다`() {
            // Given
            // User A: 원피스-루피(HAVE), 원피스-조로(WISH)
            // User B: 나루토-조로(HAVE), 나루토-루피(WISH) <- 시리즈가 다름
            val userA = UserFixture.createUser(nickname = "userA", email = "a@test.com")
            val userB = UserFixture.createUser(nickname = "userB", email = "b@test.com")

            val userAHave = createItem(1L, userA, "원피스", "루피", ItemType.HAVE)
            val userAWish = createItem(2L, userA, "원피스", "조로", ItemType.WISH)

            given(userRepository.findById(userA.id)).willReturn(userA)
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.HAVE))
                .willReturn(listOf(userAHave))
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.WISH))
                .willReturn(listOf(userAWish))
            // 같은 시리즈에서 루피를 WISH하는 사람 없음
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.WISH, "원피스", "루피"
            )).willReturn(emptyList())

            // When
            val result = matchingService.findMatches(userA.id)

            // Then
            assertThat(result).isEmpty()
        }

        @Test
        fun `여러 매칭 결과를 반환할 수 있다`() {
            // Given
            val userA = UserFixture.createUser(nickname = "userA", email = "a@test.com")
            val userB = UserFixture.createUser(nickname = "userB", email = "b@test.com")
            val userC = UserFixture.createUser(nickname = "userC", email = "c@test.com")

            val userAHave = createItem(1L, userA, "원피스", "루피", ItemType.HAVE)
            val userAWish = createItem(2L, userA, "원피스", "조로", ItemType.WISH)

            val userBHave = createItem(3L, userB, "원피스", "조로", ItemType.HAVE)
            val userBWish = createItem(4L, userB, "원피스", "루피", ItemType.WISH)

            val userCHave = createItem(5L, userC, "원피스", "조로", ItemType.HAVE)
            val userCWish = createItem(6L, userC, "원피스", "루피", ItemType.WISH)

            given(userRepository.findById(userA.id)).willReturn(userA)
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.HAVE))
                .willReturn(listOf(userAHave))
            given(itemRepository.findByOwnerIdAndType(userA.id, ItemType.WISH))
                .willReturn(listOf(userAWish))
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.WISH, "원피스", "루피"
            )).willReturn(listOf(userBWish, userCWish))
            given(itemRepository.findAvailableByTypeAndSeriesNameAndItemName(
                ItemType.HAVE, "원피스", "조로"
            )).willReturn(listOf(userBHave, userCHave))

            // When
            val result = matchingService.findMatches(userA.id)

            // Then
            assertThat(result).hasSize(2)
        }

        @Test
        fun `존재하지 않는 사용자로 매칭 조회시 실패한다`() {
            // Given
            val userId = UUID.randomUUID()
            given(userRepository.findById(userId)).willReturn(null)

            // When & Then
            assertThrows<EntityNotFoundException> {
                matchingService.findMatches(userId)
            }
        }

        @Test
        fun `HAVE 아이템이 없으면 빈 리스트를 반환한다`() {
            // Given
            val user = UserFixture.createUser()
            given(userRepository.findById(user.id)).willReturn(user)
            given(itemRepository.findByOwnerIdAndType(user.id, ItemType.HAVE))
                .willReturn(emptyList())

            // When
            val result = matchingService.findMatches(user.id)

            // Then
            assertThat(result).isEmpty()
        }
    }

    private fun createItem(
        id: Long,
        owner: com.gotchan.domain.user.model.User,
        seriesName: String,
        itemName: String,
        type: ItemType
    ) = GachaItem(
        id = id,
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        type = type,
        status = ItemStatus.AVAILABLE
    )
}
