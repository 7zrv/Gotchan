package com.gotchan.application.item.dto

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import java.time.LocalDateTime
import java.util.*

data class ItemResponse(
    val id: Long,
    val ownerId: UUID,
    val ownerNickname: String,
    val seriesName: String,
    val itemName: String,
    val imageUrl: String?,
    val status: ItemStatus,
    val type: ItemType,
    val createdAt: LocalDateTime
) {
    companion object {
        fun from(item: GachaItem) = ItemResponse(
            id = item.id,
            ownerId = item.owner.id,
            ownerNickname = item.owner.nickname,
            seriesName = item.seriesName,
            itemName = item.itemName,
            imageUrl = item.imageUrl,
            status = item.status,
            type = item.type,
            createdAt = item.createdAt
        )
    }
}

data class MatchedItemResponse(
    val myHaveItem: ItemResponse,
    val partnerWishItem: ItemResponse,
    val partnerHaveItem: ItemResponse,
    val myWishItem: ItemResponse,
    val partnerTrustScore: java.math.BigDecimal
)
