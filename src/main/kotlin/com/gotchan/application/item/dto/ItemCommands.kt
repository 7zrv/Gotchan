package com.gotchan.application.item.dto

import com.gotchan.domain.item.model.ItemType
import java.util.*

data class CreateItemCommand(
    val ownerId: UUID,
    val seriesName: String,
    val itemName: String,
    val imageUrl: String?,
    val type: ItemType
)

data class UpdateItemCommand(
    val itemId: Long,
    val requesterId: UUID,
    val seriesName: String?,
    val itemName: String?,
    val imageUrl: String?
)

data class DeleteItemCommand(
    val itemId: Long,
    val requesterId: UUID
)
