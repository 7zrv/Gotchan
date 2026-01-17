package com.gotchan.fixture

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.item.model.ItemStatus
import com.gotchan.domain.item.model.ItemType
import com.gotchan.domain.user.model.User

object ItemFixture {

    fun createHaveItem(
        id: Long = 0L,
        owner: User = UserFixture.createUser(),
        seriesName: String = "원피스",
        itemName: String = "루피",
        imageUrl: String? = null,
        status: ItemStatus = ItemStatus.AVAILABLE
    ) = GachaItem(
        id = id,
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        imageUrl = imageUrl,
        status = status,
        type = ItemType.HAVE
    )

    fun createWishItem(
        id: Long = 0L,
        owner: User = UserFixture.createUser(),
        seriesName: String = "원피스",
        itemName: String = "조로",
        imageUrl: String? = null,
        status: ItemStatus = ItemStatus.AVAILABLE
    ) = GachaItem(
        id = id,
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        imageUrl = imageUrl,
        status = status,
        type = ItemType.WISH
    )

    fun createTradingItem(
        owner: User = UserFixture.createUser(),
        seriesName: String = "원피스",
        itemName: String = "루피"
    ) = createHaveItem(
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        status = ItemStatus.TRADING
    )

    fun createCompletedItem(
        owner: User = UserFixture.createUser(),
        seriesName: String = "원피스",
        itemName: String = "루피"
    ) = createHaveItem(
        owner = owner,
        seriesName = seriesName,
        itemName = itemName,
        status = ItemStatus.COMPLETED
    )
}
