package com.gotchan.domain.item.model

import com.gotchan.common.domain.BaseEntity
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.user.model.User
import jakarta.persistence.*

@Entity
@Table(name = "gacha_items")
class GachaItem(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "owner_id", nullable = false)
    val owner: User,

    @Column(nullable = false, length = 100)
    var seriesName: String,

    @Column(nullable = false, length = 100)
    var itemName: String,

    @Column(length = 500)
    var imageUrl: String? = null,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: ItemStatus = ItemStatus.AVAILABLE,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 10)
    val type: ItemType

) : BaseEntity() {

    fun isAvailable(): Boolean = status == ItemStatus.AVAILABLE

    fun startTrading() {
        if (status != ItemStatus.AVAILABLE) {
            throw InvalidStateException("Item is not available for trading")
        }
        status = ItemStatus.TRADING
    }

    fun cancelTrading() {
        if (status != ItemStatus.TRADING) {
            throw InvalidStateException("Item is not in trading status")
        }
        status = ItemStatus.AVAILABLE
    }

    fun completeTrading() {
        if (status != ItemStatus.TRADING) {
            throw InvalidStateException("Item is not in trading status")
        }
        status = ItemStatus.COMPLETED
    }

    fun updateInfo(seriesName: String, itemName: String, imageUrl: String?) {
        if (!isAvailable()) {
            throw InvalidStateException("Cannot update item that is not available")
        }
        this.seriesName = seriesName
        this.itemName = itemName
        this.imageUrl = imageUrl
    }
}

enum class ItemStatus {
    AVAILABLE,
    TRADING,
    COMPLETED
}

enum class ItemType {
    HAVE,
    WISH
}
