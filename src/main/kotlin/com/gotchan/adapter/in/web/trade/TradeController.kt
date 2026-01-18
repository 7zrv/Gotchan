package com.gotchan.adapter.`in`.web.trade

import com.gotchan.adapter.`in`.web.trade.dto.RegisterTrackingRequest
import com.gotchan.adapter.`in`.web.trade.dto.RequestTradeRequest
import com.gotchan.adapter.`in`.web.trade.dto.RespondTradeRequest
import com.gotchan.application.trade.dto.CancelTradeCommand
import com.gotchan.application.trade.dto.ConfirmTradeCommand
import com.gotchan.application.trade.port.TradeUseCase
import com.gotchan.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1")
class TradeController(
    private val tradeUseCase: TradeUseCase
) {

    @PostMapping("/users/{userId}/trades")
    fun requestTrade(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: RequestTradeRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.requestTrade(request.toCommand(userId))
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @PostMapping("/users/{userId}/trades/{tradeId}/respond")
    fun respondTrade(
        @PathVariable userId: UUID,
        @PathVariable tradeId: Long,
        @Valid @RequestBody request: RespondTradeRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.respondTrade(request.toCommand(tradeId, userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/users/{userId}/trades/{tradeId}/tracking")
    fun registerTracking(
        @PathVariable userId: UUID,
        @PathVariable tradeId: Long,
        @Valid @RequestBody request: RegisterTrackingRequest
    ): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.registerTracking(request.toCommand(tradeId, userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/users/{userId}/trades/{tradeId}/confirm")
    fun confirmTrade(
        @PathVariable userId: UUID,
        @PathVariable tradeId: Long
    ): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.confirmTrade(ConfirmTradeCommand(tradeId, userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PostMapping("/users/{userId}/trades/{tradeId}/cancel")
    fun cancelTrade(
        @PathVariable userId: UUID,
        @PathVariable tradeId: Long
    ): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.cancelTrade(CancelTradeCommand(tradeId, userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/trades/{tradeId}")
    fun getTrade(@PathVariable tradeId: Long): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.getTrade(tradeId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @GetMapping("/users/{userId}/trades")
    fun getTradesByUser(@PathVariable userId: UUID): ResponseEntity<ApiResponse<Any>> {
        val response = tradeUseCase.getTradesByUser(userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
