package com.gotchan.domain.item.port

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import java.util.*

interface ItemRepository {
    fun save(item: GachaItem): GachaItem
    fun findById(id: Long): GachaItem?
    fun findByOwnerId(ownerId: UUID): List<GachaItem>
    fun findByOwnerIdAndType(ownerId: UUID, type: ItemType): List<GachaItem>
    fun findBySeriesName(seriesName: String): List<GachaItem>
    fun findBySeriesNameAndStatus(seriesName: String, status: ItemStatus): List<GachaItem>
    fun findAvailableByTypeAndSeriesName(type: ItemType, seriesName: String): List<GachaItem>
    fun delete(item: GachaItem)
}
