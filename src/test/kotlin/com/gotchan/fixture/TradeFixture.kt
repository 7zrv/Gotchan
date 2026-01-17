package com.gotchan.fixture

import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.model.TradeStatus
import com.gotchan.domain.user.model.User

object TradeFixture {

    fun createPendingTrade(
        id: Long = 0L,
        proposer: User = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com"),
        receiver: User = UserFixture.createUser(nickname = "receiver", email = "receiver@test.com"),
        proposerItem: GachaItem = ItemFixture.createHaveItem(owner = proposer),
        receiverItem: GachaItem = ItemFixture.createHaveItem(owner = receiver, itemName = "조로")
    ) = Trade(
        id = id,
        proposer = proposer,
        receiver = receiver,
        proposerItem = proposerItem,
        receiverItem = receiverItem,
        status = TradeStatus.PENDING
    )

    fun createAcceptedTrade(
        proposer: User = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com"),
        receiver: User = UserFixture.createUser(nickname = "receiver", email = "receiver@test.com")
    ): Trade {
        val proposerItem = ItemFixture.createTradingItem(owner = proposer)
        val receiverItem = ItemFixture.createTradingItem(owner = receiver, itemName = "조로")
        return Trade(
            proposer = proposer,
            receiver = receiver,
            proposerItem = proposerItem,
            receiverItem = receiverItem,
            status = TradeStatus.ACCEPTED
        )
    }

    fun createShippingTrade(
        proposer: User = UserFixture.createUser(nickname = "proposer", email = "proposer@test.com"),
        receiver: User = UserFixture.createUser(nickname = "receiver", email = "receiver@test.com"),
        proposerTrackingNumber: String? = "1234567890",
        receiverTrackingNumber: String? = "0987654321"
    ): Trade {
        val proposerItem = ItemFixture.createTradingItem(owner = proposer)
        val receiverItem = ItemFixture.createTradingItem(owner = receiver, itemName = "조로")
        return Trade(
            proposer = proposer,
            receiver = receiver,
            proposerItem = proposerItem,
            receiverItem = receiverItem,
            status = TradeStatus.SHIPPING,
            proposerTrackingNumber = proposerTrackingNumber,
            receiverTrackingNumber = receiverTrackingNumber
        )
    }
}
