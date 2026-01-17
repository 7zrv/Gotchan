package com.gotchan.adapter.out.persistence.trade

import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface TradeJpaRepository : JpaRepository<Trade, Long> {
    fun findByProposerId(proposerId: UUID): List<Trade>
    fun findByReceiverId(receiverId: UUID): List<Trade>

    @Query("SELECT t FROM Trade t WHERE t.proposer.id = :userId OR t.receiver.id = :userId")
    fun findByProposerIdOrReceiverId(userId: UUID): List<Trade>

    fun findByStatus(status: TradeStatus): List<Trade>

    fun existsByProposerItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean
    fun existsByReceiverItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean
}
