package com.gotchan.adapter.out.persistence.trade

import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import com.gotchan.domain.trade.port.TradeRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TradeRepositoryAdapter(
    private val tradeJpaRepository: TradeJpaRepository,
    private val tradeQueryRepository: TradeQueryRepository
) : TradeRepository {

    override fun save(trade: Trade): Trade = tradeJpaRepository.save(trade)

    override fun findById(id: Long): Trade? = tradeJpaRepository.findByIdOrNull(id)

    override fun findByProposerId(proposerId: UUID): List<Trade> =
        tradeQueryRepository.findByProposerId(proposerId)

    override fun findByReceiverId(receiverId: UUID): List<Trade> =
        tradeQueryRepository.findByReceiverId(receiverId)

    override fun findByProposerIdOrReceiverId(userId: UUID): List<Trade> =
        tradeQueryRepository.findByProposerIdOrReceiverId(userId)

    override fun findByStatus(status: TradeStatus): List<Trade> =
        tradeQueryRepository.findByStatus(status)

    override fun existsByProposerItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean =
        tradeQueryRepository.existsByProposerItemIdAndStatusIn(itemId, statuses)

    override fun existsByReceiverItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean =
        tradeQueryRepository.existsByReceiverItemIdAndStatusIn(itemId, statuses)
}
