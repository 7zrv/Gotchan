package com.gotchan.adapter.`in`.web.item

import com.gotchan.adapter.`in`.web.item.dto.CreateItemRequest
import com.gotchan.adapter.`in`.web.item.dto.UpdateItemRequest
import com.gotchan.application.item.dto.DeleteItemCommand
import com.gotchan.application.item.port.ItemUseCase
import com.gotchan.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class ItemController(
    private val itemUseCase: ItemUseCase
) {

    @PostMapping("/users/{userId}/items")
    fun createItem(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: CreateItemRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = itemUseCase.createItem(request.toCommand(userId))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @GetMapping("/items/{itemId}")
    fun getItem(@PathVariable itemId: Long): ResponseEntity<ApiResponse<Any>> {
        val response = itemUseCase.getItem(itemId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/users/{userId}/items")
    fun getItemsByOwner(@PathVariable userId: UUID): ResponseEntity<ApiResponse<Any>> {
        val response = itemUseCase.getItemsByOwner(userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/items/search")
    fun searchItems(@RequestParam seriesName: String): ResponseEntity<ApiResponse<Any>> {
        val response = itemUseCase.searchBySeries(seriesName)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/users/{userId}/items/{itemId}")
    fun updateItem(
        @PathVariable userId: UUID,
        @PathVariable itemId: Long,
        @Valid @RequestBody request: UpdateItemRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = itemUseCase.updateItem(request.toCommand(itemId, userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @DeleteMapping("/users/{userId}/items/{itemId}")
    fun deleteItem(
        @PathVariable userId: UUID,
        @PathVariable itemId: Long
    ): ResponseEntity<Void> {
        itemUseCase.deleteItem(DeleteItemCommand(itemId = itemId, requesterId = userId))
        return ResponseEntity.noContent().build()
    }
}
