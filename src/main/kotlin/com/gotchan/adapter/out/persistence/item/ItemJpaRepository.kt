package com.gotchan.adapter.out.persistence.item

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import org.springframework.data.jpa.repository.JpaRepository
import org.springframework.data.jpa.repository.Query
import java.util.*

interface ItemJpaRepository : JpaRepository<GachaItem, Long> {
    fun findByOwnerId(ownerId: UUID): List<GachaItem>
    fun findByOwnerIdAndType(ownerId: UUID, type: ItemType): List<GachaItem>
    fun findBySeriesName(seriesName: String): List<GachaItem>
    fun findBySeriesNameAndStatus(seriesName: String, status: ItemStatus): List<GachaItem>

    @Query("SELECT i FROM GachaItem i WHERE i.type = :type AND i.seriesName = :seriesName AND i.status = 'AVAILABLE'")
    fun findAvailableByTypeAndSeriesName(type: ItemType, seriesName: String): List<GachaItem>

    @Query("SELECT i FROM GachaItem i WHERE i.type = :type AND i.seriesName = :seriesName AND i.itemName = :itemName AND i.status = 'AVAILABLE'")
    fun findAvailableByTypeAndSeriesNameAndItemName(type: ItemType, seriesName: String, itemName: String): List<GachaItem>
}
