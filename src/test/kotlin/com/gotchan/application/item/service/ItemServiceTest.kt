package com.gotchan.application.item.service

import com.gotchan.application.item.dto.CreateItemCommand
import com.gotchan.application.item.dto.DeleteItemCommand
import com.gotchan.application.item.dto.UpdateItemCommand
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.common.exception.ForbiddenException
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.user.model.User
import com.gotchan.domain.user.port.UserRepository
import com.gotchan.fixture.ItemFixture
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
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.never
import org.mockito.kotlin.verify
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("ItemService")
class ItemServiceTest {

    @Mock
    lateinit var itemRepository: ItemRepository

    @Mock
    lateinit var userRepository: UserRepository

    lateinit var itemService: ItemService

    @BeforeEach
    fun setUp() {
        itemService = ItemService(itemRepository, userRepository)
    }

    @Nested
    @DisplayName("아이템 등록")
    inner class CreateItemTest {

        @Test
        fun `보유 아이템을 등록할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val command = CreateItemCommand(
                ownerId = user.id,
                seriesName = "원피스",
                itemName = "루피",
                imageUrl = "https://example.com/image.jpg",
                type = ItemType.HAVE
            )
            given(userRepository.findById(user.id)).willReturn(user)
            given(itemRepository.save(any<GachaItem>())).willAnswer { it.arguments[0] as GachaItem }

            // When
            val result = itemService.createItem(command)

            // Then
            assertThat(result.seriesName).isEqualTo("원피스")
            assertThat(result.itemName).isEqualTo("루피")
            assertThat(result.type).isEqualTo(ItemType.HAVE)
            assertThat(result.status).isEqualTo(ItemStatus.AVAILABLE)
        }

        @Test
        fun `위시 아이템을 등록할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val command = CreateItemCommand(
                ownerId = user.id,
                seriesName = "원피스",
                itemName = "조로",
                imageUrl = null,
                type = ItemType.WISH
            )
            given(userRepository.findById(user.id)).willReturn(user)
            given(itemRepository.save(any<GachaItem>())).willAnswer { it.arguments[0] as GachaItem }

            // When
            val result = itemService.createItem(command)

            // Then
            assertThat(result.type).isEqualTo(ItemType.WISH)
        }

        @Test
        fun `존재하지 않는 사용자는 아이템을 등록할 수 없다`() {
            // Given
            val userId = UUID.randomUUID()
            val command = CreateItemCommand(
                ownerId = userId,
                seriesName = "원피스",
                itemName = "루피",
                imageUrl = null,
                type = ItemType.HAVE
            )
            given(userRepository.findById(userId)).willReturn(null)

            // When & Then
            assertThrows<EntityNotFoundException> {
                itemService.createItem(command)
            }
        }
    }

    @Nested
    @DisplayName("아이템 조회")
    inner class GetItemTest {

        @Test
        fun `ID로 아이템을 조회할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val item = ItemFixture.createHaveItem(id = 1L, owner = user)
            given(itemRepository.findById(1L)).willReturn(item)

            // When
            val result = itemService.getItem(1L)

            // Then
            assertThat(result.id).isEqualTo(1L)
            assertThat(result.seriesName).isEqualTo("원피스")
        }

        @Test
        fun `존재하지 않는 아이템 조회시 실패한다`() {
            // Given
            given(itemRepository.findById(999L)).willReturn(null)

            // When & Then
            assertThrows<EntityNotFoundException> {
                itemService.getItem(999L)
            }
        }

        @Test
        fun `사용자의 아이템 목록을 조회할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val items = listOf(
                ItemFixture.createHaveItem(id = 1L, owner = user),
                ItemFixture.createWishItem(id = 2L, owner = user)
            )
            given(itemRepository.findByOwnerId(user.id)).willReturn(items)

            // When
            val result = itemService.getItemsByOwner(user.id)

            // Then
            assertThat(result).hasSize(2)
        }

        @Test
        fun `시리즈명으로 아이템을 검색할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val items = listOf(
                ItemFixture.createHaveItem(id = 1L, owner = user, seriesName = "원피스"),
                ItemFixture.createHaveItem(id = 2L, owner = user, seriesName = "원피스", itemName = "조로")
            )
            given(itemRepository.findBySeriesName("원피스")).willReturn(items)

            // When
            val result = itemService.searchBySeries("원피스")

            // Then
            assertThat(result).hasSize(2)
            assertThat(result).allMatch { it.seriesName == "원피스" }
        }
    }

    @Nested
    @DisplayName("아이템 수정")
    inner class UpdateItemTest {

        @Test
        fun `소유자는 아이템 정보를 수정할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val item = ItemFixture.createHaveItem(id = 1L, owner = user)
            val command = UpdateItemCommand(
                itemId = 1L,
                requesterId = user.id,
                seriesName = "나루토",
                itemName = "사스케",
                imageUrl = "https://new-image.com"
            )
            given(itemRepository.findById(1L)).willReturn(item)
            given(itemRepository.save(any<GachaItem>())).willAnswer { it.arguments[0] as GachaItem }

            // When
            val result = itemService.updateItem(command)

            // Then
            assertThat(result.seriesName).isEqualTo("나루토")
            assertThat(result.itemName).isEqualTo("사스케")
        }

        @Test
        fun `소유자가 아니면 아이템을 수정할 수 없다`() {
            // Given
            val owner = UserFixture.createUser()
            val otherUser = UserFixture.createUser(id = UUID.randomUUID())
            val item = ItemFixture.createHaveItem(id = 1L, owner = owner)
            val command = UpdateItemCommand(
                itemId = 1L,
                requesterId = otherUser.id,
                seriesName = "나루토",
                itemName = "사스케",
                imageUrl = null
            )
            given(itemRepository.findById(1L)).willReturn(item)

            // When & Then
            assertThrows<ForbiddenException> {
                itemService.updateItem(command)
            }
        }

        @Test
        fun `거래 중인 아이템은 수정할 수 없다`() {
            // Given
            val user = UserFixture.createUser()
            val item = ItemFixture.createTradingItem(owner = user)
            val command = UpdateItemCommand(
                itemId = item.id,
                requesterId = user.id,
                seriesName = "나루토",
                itemName = "사스케",
                imageUrl = null
            )
            given(itemRepository.findById(item.id)).willReturn(item)

            // When & Then
            assertThrows<InvalidStateException> {
                itemService.updateItem(command)
            }
        }
    }

    @Nested
    @DisplayName("아이템 삭제")
    inner class DeleteItemTest {

        @Test
        fun `소유자는 아이템을 삭제할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val item = ItemFixture.createHaveItem(id = 1L, owner = user)
            val command = DeleteItemCommand(itemId = 1L, requesterId = user.id)
            given(itemRepository.findById(1L)).willReturn(item)

            // When
            itemService.deleteItem(command)

            // Then
            verify(itemRepository).delete(item)
        }

        @Test
        fun `소유자가 아니면 아이템을 삭제할 수 없다`() {
            // Given
            val owner = UserFixture.createUser()
            val otherUser = UserFixture.createUser(id = UUID.randomUUID())
            val item = ItemFixture.createHaveItem(id = 1L, owner = owner)
            val command = DeleteItemCommand(itemId = 1L, requesterId = otherUser.id)
            given(itemRepository.findById(1L)).willReturn(item)

            // When & Then
            assertThrows<ForbiddenException> {
                itemService.deleteItem(command)
            }
            verify(itemRepository, never()).delete(any())
        }

        @Test
        fun `거래 중인 아이템은 삭제할 수 없다`() {
            // Given
            val user = UserFixture.createUser()
            val item = ItemFixture.createTradingItem(owner = user)
            val command = DeleteItemCommand(itemId = item.id, requesterId = user.id)
            given(itemRepository.findById(item.id)).willReturn(item)

            // When & Then
            assertThrows<InvalidStateException> {
                itemService.deleteItem(command)
            }
            verify(itemRepository, never()).delete(any())
        }
    }
}
