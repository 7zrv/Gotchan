package com.gotchan.adapter.out.persistence.trade

import com.gotchan.domain.item.model.QGachaItem
import com.gotchan.domain.trade.model.QTrade
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import com.gotchan.domain.user.model.QUser
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class TradeQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val trade = QTrade.trade
    private val proposer = QUser("proposer")
    private val receiver = QUser("receiver")
    private val proposerItem = QGachaItem("proposerItem")
    private val receiverItem = QGachaItem("receiverItem")

    fun findByProposerId(proposerId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .leftJoin(trade.proposer, proposer).fetchJoin()
            .leftJoin(trade.receiver, receiver).fetchJoin()
            .leftJoin(trade.proposerItem, proposerItem).fetchJoin()
            .leftJoin(trade.receiverItem, receiverItem).fetchJoin()
            .where(trade.proposer.id.eq(proposerId))
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun findByReceiverId(receiverId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .leftJoin(trade.proposer, proposer).fetchJoin()
            .leftJoin(trade.receiver, receiver).fetchJoin()
            .leftJoin(trade.proposerItem, proposerItem).fetchJoin()
            .leftJoin(trade.receiverItem, receiverItem).fetchJoin()
            .where(trade.receiver.id.eq(receiverId))
            .orderBy(trade.createdAt.desc())
            .fetch()
    }

    fun findByProposerIdOrReceiverId(userId: UUID): List<Trade> {
        return queryFactory
            .selectFrom(trade)
            .leftJoin(trade.proposer, proposer).fetchJoin()
            .leftJoin(trade.receiver, receiver).fetchJoin()
            .leftJoin(trade.proposerItem, proposerItem).fetchJoin()
            .leftJoin(trade.receiverItem, receiverItem).fetchJoin()
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
            .leftJoin(trade.proposer, proposer).fetchJoin()
            .leftJoin(trade.receiver, receiver).fetchJoin()
            .leftJoin(trade.proposerItem, proposerItem).fetchJoin()
            .leftJoin(trade.receiverItem, receiverItem).fetchJoin()
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
}
