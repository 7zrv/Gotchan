package com.gotchan.adapter.`in`.web.item.dto

import com.gotchan.application.item.dto.CreateItemCommand
import com.gotchan.application.item.dto.UpdateItemCommand
import com.gotchan.domain.item.model.ItemType
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.NotNull
import jakarta.validation.constraints.Size
import java.util.*

data class CreateItemRequest(
    @field:NotBlank(message = "시리즈명은 필수입니다")
    @field:Size(max = 100, message = "시리즈명은 100자 이내여야 합니다")
    val seriesName: String,

    @field:NotBlank(message = "아이템명은 필수입니다")
    @field:Size(max = 100, message = "아이템명은 100자 이내여야 합니다")
    val itemName: String,

    val imageUrl: String?,

    @field:NotNull(message = "타입은 필수입니다")
    val type: ItemType
) {
    fun toCommand(ownerId: UUID) = CreateItemCommand(
        ownerId = ownerId,
        seriesName = seriesName,
        itemName = itemName,
        imageUrl = imageUrl,
        type = type
    )
}

data class UpdateItemRequest(
    @field:Size(max = 100, message = "시리즈명은 100자 이내여야 합니다")
    val seriesName: String?,

    @field:Size(max = 100, message = "아이템명은 100자 이내여야 합니다")
    val itemName: String?,

    val imageUrl: String?
) {
    fun toCommand(itemId: Long, requesterId: UUID) = UpdateItemCommand(
        itemId = itemId,
        requesterId = requesterId,
        seriesName = seriesName,
        itemName = itemName,
        imageUrl = imageUrl
    )
}
