package com.gotchan.application.trade.dto

import com.gotchan.application.item.dto.ItemResponse
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import java.time.LocalDateTime
import java.util.*

data class TradeResponse(
    val id: Long,
    val proposerId: UUID,
    val proposerNickname: String,
    val receiverId: UUID,
    val receiverNickname: String,
    val proposerItem: ItemResponse,
    val receiverItem: ItemResponse,
    val status: TradeStatus,
    val proposerTrackingNumber: String?,
    val receiverTrackingNumber: String?,
    val proposerConfirmed: Boolean,
    val receiverConfirmed: Boolean,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(trade: Trade) = TradeResponse(
            id = trade.id,
            proposerId = trade.proposer.id,
            proposerNickname = trade.proposer.nickname,
            receiverId = trade.receiver.id,
            receiverNickname = trade.receiver.nickname,
            proposerItem = ItemResponse.from(trade.proposerItem),
            receiverItem = ItemResponse.from(trade.receiverItem),
            status = trade.status,
            proposerTrackingNumber = trade.proposerTrackingNumber,
            receiverTrackingNumber = trade.receiverTrackingNumber,
            proposerConfirmed = trade.proposerConfirmed,
            receiverConfirmed = trade.receiverConfirmed,
            createdAt = trade.createdAt
        )
    }
}
