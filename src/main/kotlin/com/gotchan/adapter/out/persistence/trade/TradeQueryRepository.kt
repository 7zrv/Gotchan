package com.gotchan.adapter.out.persistence.trade

import com.gotchan.domain.trade.model.QTrade
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TradeQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val trade = QTrade.trade

    fun findByProposerId(proposerId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .where(trade.proposer.id.eq(proposerId))
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun findByReceiverId(receiverId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .where(trade.receiver.id.eq(receiverId))
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun findByProposerIdOrReceiverId(userId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .where(
                trade.proposer.id.eq(userId)
                    .or(trade.receiver.id.eq(userId))
            )
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun findByStatus(status: TradeStatus): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .where(trade.status.eq(status))
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun existsByProposerItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean {
        return queryFactory
            .selectFrom(trade)
            .where(
                trade.proposerItem.id.eq(itemId),
                trade.status.`in`(statuses)
            )
            .fetchFirst() != null
    }

    fun existsByReceiverItemIdAndStatusIn(itemId: Long, statuses: List<TradeStatus>): Boolean {
        return queryFactory
            .selectFrom(trade)
            .where(
                trade.receiverItem.id.eq(itemId),
                trade.status.`in`(statuses)
            )
            .fetchFirst() != null
    }

    fun findByUserIdAndStatus(userId: UUID, status: TradeStatus): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .where(
                trade.proposer.id.eq(userId)
                    .or(trade.receiver.id.eq(userId)),
                trade.status.eq(status)
            )
            .orderBy(trade.createdAt.desc())
            .fetch()
    }
}
