package com.gotchan.application.trade.service

import com.gotchan.application.trade.dto.*
import com.gotchan.common.exception.BusinessException
import com.gotchan.common.exception.ForbiddenException
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import com.gotchan.domain.trade.port.TradeRepository
import com.gotchan.domain.user.port.UserRepository
import com.gotchan.fixture.ItemFixture
import com.gotchan.fixture.TradeFixture
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
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("TradeService")
class TradeServiceTest {

    @Mock
    lateinit var tradeRepository: TradeRepository

    @Mock
    lateinit var itemRepository: ItemRepository

    @Mock
    lateinit var userRepository: UserRepository

    lateinit var tradeService: TradeService

    @BeforeEach
    fun setUp() {
        tradeService = TradeService(tradeRepository, itemRepository, userRepository)
    }

    @Nested
    @DisplayName("교환 요청")
    inner class RequestTradeTest {

        @Test
        fun `교환을 요청할 수 있다`() {
            // Given
            val proposer = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com")
            val receiver = UserFixture.createUser(nickname = "receiver", email = "receiver@test.com")
            val proposerItem = ItemFixture.createHaveItem(id = 1L, owner = proposer)
            val receiverItem = ItemFixture.createHaveItem(id = 2L, owner = receiver, itemName = "조로")

            val command = RequestTradeCommand(
                proposerId = proposer.id,
                proposerItemId = 1L,
                receiverItemId = 2L
            )

            given(userRepository.findById(proposer.id)).willReturn(proposer)
            given(itemRepository.findById(1L)).willReturn(proposerItem)
            given(itemRepository.findById(2L)).willReturn(receiverItem)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.requestTrade(command)

            // Then
            assertThat(result.status).isEqualTo(TradeStatus.PENDING)
            assertThat(result.proposerNickname).isEqualTo("proposer")
            assertThat(result.receiverNickname).isEqualTo("receiver")
        }

        @Test
        fun `자기 자신에게는 교환을 요청할 수 없다`() {
            // Given
            val user = UserFixture.createUser()
            val myItem1 = ItemFixture.createHaveItem(id = 1L, owner = user)
            val myItem2 = ItemFixture.createHaveItem(id = 2L, owner = user, itemName = "조로")

            val command = RequestTradeCommand(
                proposerId = user.id,
                proposerItemId = 1L,
                receiverItemId = 2L
            )

            given(userRepository.findById(user.id)).willReturn(user)
            given(itemRepository.findById(1L)).willReturn(myItem1)
            given(itemRepository.findById(2L)).willReturn(myItem2)

            // When & Then
            assertThrows<BusinessException> {
                tradeService.requestTrade(command)
            }
        }

        @Test
        fun `본인 소유가 아닌 아이템으로는 교환을 요청할 수 없다`() {
            // Given
            val proposer = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com")
            val otherUser = UserFixture.createUser(nickname = "other", email = "other@test.com")
            val otherUserItem = ItemFixture.createHaveItem(id = 1L, owner = otherUser)

            val command = RequestTradeCommand(
                proposerId = proposer.id,
                proposerItemId = 1L,
                receiverItemId = 2L
            )

            given(userRepository.findById(proposer.id)).willReturn(proposer)
            given(itemRepository.findById(1L)).willReturn(otherUserItem)

            // When & Then
            assertThrows<ForbiddenException> {
                tradeService.requestTrade(command)
            }
        }

        @Test
        fun `거래 중인 아이템으로는 교환을 요청할 수 없다`() {
            // Given
            val proposer = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com")
            val tradingItem = ItemFixture.createTradingItem(owner = proposer)

            val command = RequestTradeCommand(
                proposerId = proposer.id,
                proposerItemId = tradingItem.id,
                receiverItemId = 2L
            )

            given(userRepository.findById(proposer.id)).willReturn(proposer)
            given(itemRepository.findById(tradingItem.id)).willReturn(tradingItem)

            // When & Then
            assertThrows<InvalidStateException> {
                tradeService.requestTrade(command)
            }
        }
    }

    @Nested
    @DisplayName("교환 수락/거절")
    inner class RespondTradeTest {

        @Test
        fun `수신자는 교환을 수락할 수 있다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            val command = RespondTradeCommand(
                tradeId = 1L,
                responderId = trade.receiver.id,
                accept = true
            )

            given(tradeRepository.findById(1L)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.respondTrade(command)

            // Then
            assertThat(result.status).isEqualTo(TradeStatus.ACCEPTED)
        }

        @Test
        fun `수신자는 교환을 거절할 수 있다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            val command = RespondTradeCommand(
                tradeId = 1L,
                responderId = trade.receiver.id,
                accept = false
            )

            given(tradeRepository.findById(1L)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.respondTrade(command)

            // Then
            assertThat(result.status).isEqualTo(TradeStatus.CANCELLED)
        }

        @Test
        fun `수신자가 아니면 교환에 응답할 수 없다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            val otherUser = UUID.randomUUID()
            val command = RespondTradeCommand(
                tradeId = 1L,
                responderId = otherUser,
                accept = true
            )

            given(tradeRepository.findById(1L)).willReturn(trade)

            // When & Then
            assertThrows<ForbiddenException> {
                tradeService.respondTrade(command)
            }
        }
    }

    @Nested
    @DisplayName("운송장 등록")
    inner class RegisterTrackingTest {

        @Test
        fun `제안자가 운송장을 등록할 수 있다`() {
            // Given
            val trade = TradeFixture.createAcceptedTrade()
            val command = RegisterTrackingCommand(
                tradeId = trade.id,
                userId = trade.proposer.id,
                trackingNumber = "1234567890"
            )

            given(tradeRepository.findById(trade.id)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.registerTracking(command)

            // Then
            assertThat(result.proposerTrackingNumber).isEqualTo("1234567890")
            assertThat(result.status).isEqualTo(TradeStatus.SHIPPING)
        }

        @Test
        fun `수신자가 운송장을 등록할 수 있다`() {
            // Given
            val trade = TradeFixture.createAcceptedTrade()
            val command = RegisterTrackingCommand(
                tradeId = trade.id,
                userId = trade.receiver.id,
                trackingNumber = "0987654321"
            )

            given(tradeRepository.findById(trade.id)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.registerTracking(command)

            // Then
            assertThat(result.receiverTrackingNumber).isEqualTo("0987654321")
        }

        @Test
        fun `거래 당사자가 아니면 운송장을 등록할 수 없다`() {
            // Given
            val trade = TradeFixture.createAcceptedTrade()
            val otherUser = UUID.randomUUID()
            val command = RegisterTrackingCommand(
                tradeId = trade.id,
                userId = otherUser,
                trackingNumber = "1234567890"
            )

            given(tradeRepository.findById(trade.id)).willReturn(trade)

            // When & Then
            assertThrows<ForbiddenException> {
                tradeService.registerTracking(command)
            }
        }
    }

    @Nested
    @DisplayName("거래 확인")
    inner class ConfirmTradeTest {

        @Test
        fun `제안자가 거래를 확인할 수 있다`() {
            // Given
            val trade = TradeFixture.createShippingTrade()
            val command = ConfirmTradeCommand(
                tradeId = trade.id,
                userId = trade.proposer.id
            )

            given(tradeRepository.findById(trade.id)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.confirmTrade(command)

            // Then
            assertThat(result.proposerConfirmed).isTrue()
        }

        @Test
        fun `양측이 모두 확인하면 거래가 완료된다`() {
            // Given
            val trade = TradeFixture.createShippingTrade()
            trade.confirmByProposer()

            val command = ConfirmTradeCommand(
                tradeId = trade.id,
                userId = trade.receiver.id
            )

            given(tradeRepository.findById(trade.id)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.confirmTrade(command)

            // Then
            assertThat(result.status).isEqualTo(TradeStatus.FINISHED)
            assertThat(result.proposerConfirmed).isTrue()
            assertThat(result.receiverConfirmed).isTrue()
        }
    }

    @Nested
    @DisplayName("거래 취소")
    inner class CancelTradeTest {

        @Test
        fun `제안자가 PENDING 상태의 거래를 취소할 수 있다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            val command = CancelTradeCommand(
                tradeId = 1L,
                userId = trade.proposer.id
            )

            given(tradeRepository.findById(1L)).willReturn(trade)
            given(tradeRepository.save(any<Trade>())).willAnswer { it.arguments[0] as Trade }

            // When
            val result = tradeService.cancelTrade(command)

            // Then
            assertThat(result.status).isEqualTo(TradeStatus.CANCELLED)
        }

        @Test
        fun `거래 당사자가 아니면 취소할 수 없다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            val otherUser = UUID.randomUUID()
            val command = CancelTradeCommand(
                tradeId = 1L,
                userId = otherUser
            )

            given(tradeRepository.findById(1L)).willReturn(trade)

            // When & Then
            assertThrows<ForbiddenException> {
                tradeService.cancelTrade(command)
            }
        }
    }

    @Nested
    @DisplayName("거래 조회")
    inner class GetTradeTest {

        @Test
        fun `ID로 거래를 조회할 수 있다`() {
            // Given
            val trade = TradeFixture.createPendingTrade(id = 1L)
            given(tradeRepository.findById(1L)).willReturn(trade)

            // When
            val result = tradeService.getTrade(1L)

            // Then
            assertThat(result.id).isEqualTo(1L)
        }

        @Test
        fun `사용자의 거래 목록을 조회할 수 있다`() {
            // Given
            val user = UserFixture.createUser()
            val trades = listOf(
                TradeFixture.createPendingTrade(id = 1L),
                TradeFixture.createPendingTrade(id = 2L)
            )
            given(tradeRepository.findByProposerIdOrReceiverId(user.id)).willReturn(trades)

            // When
            val result = tradeService.getTradesByUser(user.id)

            // Then
            assertThat(result).hasSize(2)
        }
    }
}
