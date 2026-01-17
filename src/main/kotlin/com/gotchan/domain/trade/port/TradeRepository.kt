package com.gotchan.domain.trade.port

import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import java.util.*

interface TradeRepository {
    fun save(trade: Trade): Trade
    fun findById(id: Long): Trade?
    fun findByProposerId(proposerId: UUID): List<Trade>
    fun findByReceiverId(receiverId: UUID): List<Trade>
    fun findByProposerIdOrReceiverId(userId: UUID): List<Trade>
    fun findByStatus(status: TradeStatus): List<Trade>
    fun existsByProposerItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean
    fun existsByReceiverItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean
}
