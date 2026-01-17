package com.gotchan.application.item.port

import com.gotchan.application.item.dto.*
import java.util.*

interface ItemUseCase {
    fun createItem(command: CreateItemCommand): ItemResponse
    fun getItem(itemId: Long): ItemResponse
    fun getItemsByOwner(ownerId: UUID): List<ItemResponse>
    fun searchBySeries(seriesName: String): List<ItemResponse>
    fun updateItem(command: UpdateItemCommand): ItemResponse
    fun deleteItem(command: DeleteItemCommand)
}

interface MatchingUseCase {
    fun findMatches(userId: UUID): List<MatchResult>
}
