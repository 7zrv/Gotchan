package com.gotchan.application.trade.dto

import java.util.*

data class RequestTradeCommand(
    val proposerId: UUID,
    val proposerItemId: Long,
    val receiverItemId: Long
)

data class RespondTradeCommand(
    val tradeId: Long,
    val responderId: UUID,
    val accept: Boolean
)

data class RegisterTrackingCommand(
    val tradeId: Long,
    val userId: UUID,
    val trackingNumber: String
)

data class ConfirmTradeCommand(
    val tradeId: Long,
    val userId: UUID
)

data class CancelTradeCommand(
    val tradeId: Long,
    val userId: UUID
)
