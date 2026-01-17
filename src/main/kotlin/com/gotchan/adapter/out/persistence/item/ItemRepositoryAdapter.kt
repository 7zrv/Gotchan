package com.gotchan.adapter.out.persistence.item

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.item.port.ItemRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class ItemRepositoryAdapter(
    private val itemJpaRepository: ItemJpaRepository
) : ItemRepository {

    override fun save(item: GachaItem): GachaItem = itemJpaRepository.save(item)

    override fun findById(id: Long): GachaItem? = itemJpaRepository.findByIdOrNull(id)

    override fun findByOwnerId(ownerId: UUID): List<GachaItem> = itemJpaRepository.findByOwnerId(ownerId)

    override fun findByOwnerIdAndType(ownerId: UUID, type: ItemType): List<GachaItem> =
        itemJpaRepository.findByOwnerIdAndType(ownerId, type)

    override fun findBySeriesName(seriesName: String): List<GachaItem> =
        itemJpaRepository.findBySeriesName(seriesName)

    override fun findBySeriesNameAndStatus(seriesName: String, status: ItemStatus): List<GachaItem> =
        itemJpaRepository.findBySeriesNameAndStatus(seriesName, status)

    override fun findAvailableByTypeAndSeriesName(type: ItemType, seriesName: String): List<GachaItem> =
        itemJpaRepository.findAvailableByTypeAndSeriesName(type, seriesName)

    override fun findAvailableByTypeAndSeriesNameAndItemName(
        type: ItemType,
        seriesName: String,
        itemName: String
    ): List<GachaItem> = itemJpaRepository.findAvailableByTypeAndSeriesNameAndItemName(type, seriesName, itemName)

    override fun delete(item: GachaItem) = itemJpaRepository.delete(item)
}
