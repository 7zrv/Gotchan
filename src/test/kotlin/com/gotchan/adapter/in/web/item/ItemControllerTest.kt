package com.gotchan.adapter.`in`.web.item

import tools.jackson.databind.ObjectMapper
import com.gotchan.adapter.`in`.web.item.dto.CreateItemRequest
import com.gotchan.adapter.`in`.web.item.dto.UpdateItemRequest
import com.gotchan.application.item.dto.ItemResponse
import com.gotchan.application.item.port.ItemUseCase
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.common.exception.ForbiddenException
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
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

@WebMvcTest(ItemController::class)
@DisplayName("ItemController")
class ItemControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var itemUseCase: ItemUseCase

    private val testUserId = UUID.randomUUID()
    private val testItemId = 1L

    private fun createItemResponse(
        id: Long = testItemId,
        ownerId: UUID = testUserId,
        type: ItemType = ItemType.HAVE
    ) = ItemResponse(
        id = id,
        ownerId = ownerId,
        ownerNickname = "tester",
        seriesName = "Pokemon",
        itemName = "Pikachu",
        imageUrl = "https://example.com/pikachu.png",
        status = ItemStatus.AVAILABLE,
        type = type,
        createdAt = LocalDateTime.now()
    )

    @Nested
    @DisplayName("POST /api/v1/users/{userId}/items")
    inner class CreateItemTest {

        @Test
        fun `아이템 생성 성공시 201을 반환한다`() {
            // Given
            val request = CreateItemRequest(
                seriesName = "Pokemon",
                itemName = "Pikachu",
                imageUrl = "https://example.com/pikachu.png",
                type = ItemType.HAVE
            )
            val response = createItemResponse()
            given(itemUseCase.createItem(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$testUserId/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seriesName").value("Pokemon"))
                .andExpect(jsonPath("$.data.itemName").value("Pikachu"))
        }

        @Test
        fun `시리즈명이 없으면 400을 반환한다`() {
            // Given
            val request = mapOf(
                "itemName" to "Pikachu",
                "type" to "HAVE"
            )

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$testUserId/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        fun `존재하지 않는 사용자면 404를 반환한다`() {
            // Given
            val request = CreateItemRequest(
                seriesName = "Pokemon",
                itemName = "Pikachu",
                imageUrl = null,
                type = ItemType.HAVE
            )
            given(itemUseCase.createItem(any()))
                .willThrow(EntityNotFoundException("User", testUserId))

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/$testUserId/items")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/{itemId}")
    inner class GetItemTest {

        @Test
        fun `아이템 조회 성공시 200을 반환한다`() {
            // Given
            val response = createItemResponse()
            given(itemUseCase.getItem(testItemId)).willReturn(response)

            // When & Then
            mockMvc.perform(get("/api/v1/items/$testItemId"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.id").value(testItemId))
                .andExpect(jsonPath("$.data.seriesName").value("Pokemon"))
        }

        @Test
        fun `존재하지 않는 아이템 조회시 404를 반환한다`() {
            // Given
            given(itemUseCase.getItem(testItemId))
                .willThrow(EntityNotFoundException("Item", testItemId))

            // When & Then
            mockMvc.perform(get("/api/v1/items/$testItemId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/items")
    inner class GetItemsByOwnerTest {

        @Test
        fun `사용자 아이템 목록 조회 성공시 200을 반환한다`() {
            // Given
            val items = listOf(
                createItemResponse(id = 1L, type = ItemType.HAVE),
                createItemResponse(id = 2L, type = ItemType.WISH)
            )
            given(itemUseCase.getItemsByOwner(testUserId)).willReturn(items)

            // When & Then
            mockMvc.perform(get("/api/v1/users/$testUserId/items"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
                .andExpect(jsonPath("$.data.length()").value(2))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/items/search")
    inner class SearchItemsTest {

        @Test
        fun `시리즈명으로 검색 성공시 200을 반환한다`() {
            // Given
            val items = listOf(createItemResponse())
            given(itemUseCase.searchBySeries("Pokemon")).willReturn(items)

            // When & Then
            mockMvc.perform(get("/api/v1/items/search").param("seriesName", "Pokemon"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data").isArray)
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}/items/{itemId}")
    inner class UpdateItemTest {

        @Test
        fun `아이템 수정 성공시 200을 반환한다`() {
            // Given
            val request = UpdateItemRequest(
                seriesName = "Pokemon Updated",
                itemName = null,
                imageUrl = null
            )
            val response = createItemResponse().copy(seriesName = "Pokemon Updated")
            given(itemUseCase.updateItem(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                patch("/api/v1/users/$testUserId/items/$testItemId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.seriesName").value("Pokemon Updated"))
        }

        @Test
        fun `권한이 없으면 403을 반환한다`() {
            // Given
            val request = UpdateItemRequest(seriesName = "Updated", itemName = null, imageUrl = null)
            given(itemUseCase.updateItem(any()))
                .willThrow(ForbiddenException("You are not the owner of this item"))

            // When & Then
            mockMvc.perform(
                patch("/api/v1/users/$testUserId/items/$testItemId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isForbidden)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("DELETE /api/v1/users/{userId}/items/{itemId}")
    inner class DeleteItemTest {

        @Test
        fun `아이템 삭제 성공시 204를 반환한다`() {
            // When & Then
            mockMvc.perform(delete("/api/v1/users/$testUserId/items/$testItemId"))
                .andExpect(status().isNoContent)
        }

        @Test
        fun `존재하지 않는 아이템 삭제시 404를 반환한다`() {
            // Given
            given(itemUseCase.deleteItem(any()))
                .willThrow(EntityNotFoundException("Item", testItemId))

            // When & Then
            mockMvc.perform(delete("/api/v1/users/$testUserId/items/$testItemId"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }
}
