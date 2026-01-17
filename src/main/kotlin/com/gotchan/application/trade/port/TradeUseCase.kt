package com.gotchan.application.trade.port

import com.gotchan.application.trade.dto.*
import java.util.*

interface TradeUseCase {
    fun requestTrade(command: RequestTradeCommand): TradeResponse
    fun respondTrade(command: RespondTradeCommand): TradeResponse
    fun registerTracking(command: RegisterTrackingCommand): TradeResponse
    fun confirmTrade(command: ConfirmTradeCommand): TradeResponse
    fun cancelTrade(command: CancelTradeCommand): TradeResponse
    fun getTrade(tradeId: Long): TradeResponse
    fun getTradesByUser(userId: UUID): List<TradeResponse>
}
