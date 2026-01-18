package com.gotchan.application.item.service

import com.gotchan.application.item.dto.CreateItemCommand
import com.gotchan.application.item.dto.DeleteItemCommand
import com.gotchan.application.item.dto.ItemResponse
import com.gotchan.application.item.dto.UpdateItemCommand
import com.gotchan.application.item.port.ItemUseCase
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.common.exception.ForbiddenException
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.user.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class ItemService(
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) : ItemUseCase {

    @Transactional
    override fun createItem(command: CreateItemCommand): ItemResponse {
        val owner = userRepository.findById(command.ownerId)
            ?: throw EntityNotFoundException("User", command.ownerId)

        val item = GachaItem(
            owner = owner,
            seriesName = command.seriesName,
            itemName = command.itemName,
            imageUrl = command.imageUrl,
            type = command.type
        )

        val savedItem = itemRepository.save(item)
        return ItemResponse.from(savedItem)
    }

    override fun getItem(itemId: Long): ItemResponse {
        val item = itemRepository.findById(itemId)
            ?: throw EntityNotFoundException("Item", itemId)
        return ItemResponse.from(item)
    }

    override fun getItemsByOwner(ownerId: UUID): List<ItemResponse> {
        return itemRepository.findByOwnerId(ownerId)
            .map { ItemResponse.from(it) }
    }

    override fun searchBySeries(seriesName: String): List<ItemResponse> {
        return itemRepository.findBySeriesName(seriesName)
            .map { ItemResponse.from(it) }
    }

    @Transactional
    override fun updateItem(command: UpdateItemCommand): ItemResponse {
        val item = itemRepository.findById(command.itemId)
            ?: throw EntityNotFoundException("Item", command.itemId)

        validateOwnership(item, command.requesterId)

        // 도메인 모델(GachaItem.updateInfo)에서 상태 검증 수행
        item.updateInfo(
            seriesName = command.seriesName ?: item.seriesName,
            itemName = command.itemName ?: item.itemName,
            imageUrl = command.imageUrl ?: item.imageUrl
        )

        val savedItem = itemRepository.save(item)
        return ItemResponse.from(savedItem)
    }

    @Transactional
    override fun deleteItem(command: DeleteItemCommand) {
        val item = itemRepository.findById(command.itemId)
            ?: throw EntityNotFoundException("Item", command.itemId)

        validateOwnership(item, command.requesterId)
        validateDeletable(item)

        itemRepository.delete(item)
    }

    private fun validateDeletable(item: GachaItem) {
        if (!item.isAvailable()) {
            throw InvalidStateException("Cannot delete item that is not available")
        }
    }

    private fun validateOwnership(item: GachaItem, requesterId: UUID) {
        if (item.owner.id != requesterId) {
            throw ForbiddenException("You are not the owner of this item")
        }
    }
}
