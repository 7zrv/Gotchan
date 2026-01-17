package com.gotchan.adapter.out.persistence.item

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.item.model.QGachaItem
import com.querydsl.jpa.impl.JPAQueryFactory
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ItemQueryRepository(
    private val queryFactory: JPAQueryFactory
) {
    private val item = QGachaItem.gachaItem

    fun findByOwnerId(ownerId: UUID): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(item.owner.id.eq(ownerId))
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun findByOwnerIdAndType(ownerId: UUID, type: ItemType): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(
                item.owner.id.eq(ownerId),
                item.type.eq(type)
            )
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun findBySeriesName(seriesName: String): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(item.seriesName.eq(seriesName))
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun findBySeriesNameAndStatus(seriesName: String, status: ItemStatus): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(
                item.seriesName.eq(seriesName),
                item.status.eq(status)
            )
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun findAvailableByTypeAndSeriesName(type: ItemType, seriesName: String): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(
                item.type.eq(type),
                item.seriesName.eq(seriesName),
                item.status.eq(ItemStatus.AVAILABLE)
            )
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun findAvailableByTypeAndSeriesNameAndItemName(
        type: ItemType,
        seriesName: String,
        itemName: String
    ): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(
                item.type.eq(type),
                item.seriesName.eq(seriesName),
                item.itemName.eq(itemName),
                item.status.eq(ItemStatus.AVAILABLE)
            )
            .orderBy(item.createdAt.desc())
            .fetch()
    }

    fun searchByKeyword(keyword: String): List<GachaItem> {
        return queryFactory
            .selectFrom(item)
            .where(
                item.seriesName.containsIgnoreCase(keyword)
                    .or(item.itemName.containsIgnoreCase(keyword))
            )
            .where(item.status.eq(ItemStatus.AVAILABLE))
            .orderBy(item.createdAt.desc())
            .fetch()
    }
}
