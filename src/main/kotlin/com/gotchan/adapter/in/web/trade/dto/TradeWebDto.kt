package com.gotchan.adapter.`in`.web.trade.dto

import com.gotchan.application.trade.dto.*
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import java.util.*

data class RequestTradeRequest(
    @field:NotNull(message = "제안 아이템 ID는 필수입니다")
    val proposerItemId: Long,

    @field:NotNull(message = "수신 아이템 ID는 필수입니다")
    val receiverItemId: Long
) {
    fun toCommand(proposerId: UUID) = RequestTradeCommand(
        proposerId = proposerId,
        proposerItemId = proposerItemId,
        receiverItemId = receiverItemId
    )
}

data class RespondTradeRequest(
    @field:NotNull(message = "수락 여부는 필수입니다")
    val accept: Boolean
) {
    fun toCommand(tradeId: Long, responderId: UUID) = RespondTradeCommand(
        tradeId = tradeId,
        responderId = responderId,
        accept = accept
    )
}

data class RegisterTrackingRequest(
    @field:NotBlank(message = "운송장 번호는 필수입니다")
    val trackingNumber: String
) {
    fun toCommand(tradeId: Long, userId: UUID) = RegisterTrackingCommand(
        tradeId = tradeId,
        userId = userId,
        trackingNumber = trackingNumber
    )
}
