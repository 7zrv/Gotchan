package com.gotchan.adapter.`in`.web.trade

import tools.jackson.databind.ObjectMapper
import com.gotchan.adapter.`in`.web.trade.dto.RequestTradeRequest
import com.gotchan.adapter.`in`.web.trade.dto.RespondTradeRequest
import com.gotchan.adapter.`in`.web.trade.dto.RegisterTrackingRequest
import com.gotchan.application.item.dto.ItemResponse
import com.gotchan.application.trade.dto.TradeResponse
import com.gotchan.application.trade.port.TradeUseCase
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.common.exception.ForbiddenException
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.trade.model.TradeStatus
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.time.LocalDateTime
import java.util.*

@WebMvcTest(TradeController::class)
@DisplayName("TradeController")
class TradeControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var tradeUseCase: TradeUseCase

    private val proposerId = UUID.randomUUID()
    private val receiverId = UUID.randomUUID()
    private val tradeId = 1L

    private fun createItemResponse(
        id: Long,
        ownerId: UUID,
        type: ItemType
    ) = ItemResponse(
        id = id,
        ownerId = ownerId,
        ownerNickname = "tester",
        seriesName = "Pokemon",
        itemName = "Pikachu",
        imageUrl = null,
        status = ItemStatus.AVAILABLE,
        type = type,
        createdAt = LocalDateTime.now()
    )

    private fun createTradeResponse(
        status: TradeStatus = TradeStatus.PENDING
    ) = TradeResponse(
        id = tradeId,
        proposerId = proposerId,
        proposerNickname = "proposer",
        receiverId = receiverId,
        receiverNickname = "receiver",
        proposerItem = createItemResponse(1L, proposerId, ItemType.HAVE),
        receiverItem = createItemResponse(2L, receiverId, ItemType.HAVE),
        status = status,
        proposerTrackingNumber = null,
        receiverTrackingNumber = null,
        proposerConfirmed = false,
        receiverConfirmed = false,
        createdAt = LocalDateTime.now()
    )

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/trades")
    inner class RequestTradeTest {

        @Test
        fun `거래 요청 성공시 201을 반환한다`() {
            // Given
            val request = RequestTradeRequest(
                proposerItemId = 1L,
                receiverItemId = 2L
            )
            val response = createTradeResponse()
            given(tradeUseCase.requestTrade(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$proposerId/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("PENDING"))
        }

        @Test
        fun `존재하지 않는 아이템으로 거래 요청시 404를 반환한다`() {
            // Given
            val request = RequestTradeRequest(proposerItemId = 999L, receiverItemId = 2L)
            given(tradeUseCase.requestTrade(any()))
                .willThrow(EntityNotFoundException("Item", 999L))

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$proposerId/trades")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/trades/{tradeId}/respond")
    inner class RespondTradeTest {

        @Test
        fun `거래 수락 성공시 200을 반환한다`() {
            // Given
            val request = RespondTradeRequest(accept = true)
            val response = createTradeResponse(TradeStatus.ACCEPTED)
            given(tradeUseCase.respondTrade(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$receiverId/trades/$tradeId/respond")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("ACCEPTED"))
        }

        @Test
        fun `거래 거절 성공시 200을 반환한다`() {
            // Given
            val request = RespondTradeRequest(accept = false)
            val response = createTradeResponse(TradeStatus.CANCELLED)
            given(tradeUseCase.respondTrade(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$receiverId/trades/$tradeId/respond")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
        }

        @Test
        fun `권한이 없으면 403을 반환한다`() {
            // Given
            val request = RespondTradeRequest(accept = true)
            given(tradeUseCase.respondTrade(any()))
                .willThrow(ForbiddenException("Only the receiver can respond to this trade"))

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$proposerId/trades/$tradeId/respond")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/trades/{tradeId}/tracking")
    inner class RegisterTrackingTest {

        @Test
        fun `운송장 등록 성공시 200을 반환한다`() {
            // Given
            val request = RegisterTrackingRequest(trackingNumber = "1234567890")
            val response = createTradeResponse(TradeStatus.SHIPPING).copy(
                proposerTrackingNumber = "1234567890"
            )
            given(tradeUseCase.registerTracking(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$proposerId/trades/$tradeId/tracking")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.proposerTrackingNumber").value("1234567890"))
        }

        @Test
        fun `잘못된 상태에서 운송장 등록시 400을 반환한다`() {
            // Given
            val request = RegisterTrackingRequest(trackingNumber = "1234567890")
            given(tradeUseCase.registerTracking(any()))
                .willThrow(InvalidStateException("Cannot register tracking number in current status"))

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$proposerId/trades/$tradeId/tracking")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/trades/{tradeId}/confirm")
    inner class ConfirmTradeTest {

        @Test
        fun `거래 확인 성공시 200을 반환한다`() {
            // Given
            val response = createTradeResponse(TradeStatus.SHIPPING).copy(
                proposerConfirmed = true
            )
            given(tradeUseCase.confirmTrade(any())).willReturn(response)

            // When & Then
            mockMvc.perform(post("/api/v1/users/$proposerId/trades/$tradeId/confirm"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.proposerConfirmed").value(true))
        }
    }

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/trades/{tradeId}/cancel")
    inner class CancelTradeTest {

        @Test
        fun `거래 취소 성공시 200을 반환한다`() {
            // Given
            val response = createTradeResponse(TradeStatus.CANCELLED)
            given(tradeUseCase.cancelTrade(any())).willReturn(response)

            // When & Then
            mockMvc.perform(post("/api/v1/users/$proposerId/trades/$tradeId/cancel"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.status").value("CANCELLED"))
        }

        @Test
        fun `잘못된 상태에서 취소시 400을 반환한다`() {
            // Given
            given(tradeUseCase.cancelTrade(any()))
                .willThrow(InvalidStateException("Cannot cancel trade in current status"))

            // When & Then
            mockMvc.perform(post("/api/v1/users/$proposerId/trades/$tradeId/cancel"))
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/trades/{tradeId}")
    inner class GetTradeTest {

        @Test
        fun `거래 조회 성공시 200을 반환한다`() {
            // Given
            val response = createTradeResponse()
            given(tradeUseCase.getTrade(tradeId)).willReturn(response)

            // When & Then
            mockMvc.perform(get("/api/v1/trades/$tradeId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(tradeId))
        }

        @Test
        fun `존재하지 않는 거래 조회시 404를 반환한다`() {
            // Given
            given(tradeUseCase.getTrade(tradeId))
                .willThrow(EntityNotFoundException("Trade", tradeId))

            // When & Then
            mockMvc.perform(get("/api/v1/trades/$tradeId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/trades")
    inner class GetTradesByUserTest {

        @Test
        fun `사용자 거래 목록 조회 성공시 200을 반환한다`() {
            // Given
            val trades = listOf(createTradeResponse())
            given(tradeUseCase.getTradesByUser(proposerId)).willReturn(trades)

            // When & Then
            mockMvc.perform(get("/api/v1/users/$proposerId/trades"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(1))
        }
    }
}
