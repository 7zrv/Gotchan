package com.gotchan.application.trade.service

import com.gotchan.application.trade.dto.*
import com.gotchan.common.exception.*
import com.gotchan.domain.item.port.ItemRepository
import com.gotchan.domain.trade.model.Trade
import com.gotchan.domain.trade.port.TradeRepository
import com.gotchan.domain.user.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class TradeService(
    private val tradeRepository: TradeRepository,
    private val itemRepository: ItemRepository,
    private val userRepository: UserRepository
) {

    @Transactional
    fun requestTrade(command: RequestTradeCommand): TradeResponse {
        val proposer = userRepository.findById(command.proposerId)
            ?: throw EntityNotFoundException("User", command.proposerId)

        val proposerItem = itemRepository.findById(command.proposerItemId)
            ?: throw EntityNotFoundException("Item", command.proposerItemId)

        // 본인 소유 아이템인지 먼저 확인
        if (proposerItem.owner.id != proposer.id) {
            throw ForbiddenException("You are not the owner of this item")
        }

        // 제안자 아이템 상태 확인
        if (!proposerItem.isAvailable()) {
            throw InvalidStateException("Proposer item is not available for trade")
        }

        val receiverItem = itemRepository.findById(command.receiverItemId)
            ?: throw EntityNotFoundException("Item", command.receiverItemId)

        // 자기 자신에게 교환 요청 불가
        if (receiverItem.owner.id == proposer.id) {
            throw BusinessException(ErrorCode.SELF_TRADE_NOT_ALLOWED)
        }

        // 수신자 아이템 상태 확인
        if (!receiverItem.isAvailable()) {
            throw InvalidStateException("Receiver item is not available for trade")
        }

        val trade = Trade(
            proposer = proposer,
            receiver = receiverItem.owner,
            proposerItem = proposerItem,
            receiverItem = receiverItem
        )

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    @Transactional
    fun respondTrade(command: RespondTradeCommand): TradeResponse {
        val trade = tradeRepository.findById(command.tradeId)
            ?: throw EntityNotFoundException("Trade", command.tradeId)

        if (trade.receiver.id != command.responderId) {
            throw ForbiddenException("Only the receiver can respond to this trade")
        }

        if (command.accept) {
            trade.accept()
        } else {
            trade.reject()
        }

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    @Transactional
    fun registerTracking(command: RegisterTrackingCommand): TradeResponse {
        val trade = tradeRepository.findById(command.tradeId)
            ?: throw EntityNotFoundException("Trade", command.tradeId)

        when (command.userId) {
            trade.proposer.id -> trade.registerProposerTracking(command.trackingNumber)
            trade.receiver.id -> trade.registerReceiverTracking(command.trackingNumber)
            else -> throw ForbiddenException("Only trade participants can register tracking")
        }

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    @Transactional
    fun confirmTrade(command: ConfirmTradeCommand): TradeResponse {
        val trade = tradeRepository.findById(command.tradeId)
            ?: throw EntityNotFoundException("Trade", command.tradeId)

        when (command.userId) {
            trade.proposer.id -> trade.confirmByProposer()
            trade.receiver.id -> trade.confirmByReceiver()
            else -> throw ForbiddenException("Only trade participants can confirm")
        }

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    @Transactional
    fun cancelTrade(command: CancelTradeCommand): TradeResponse {
        val trade = tradeRepository.findById(command.tradeId)
            ?: throw EntityNotFoundException("Trade", command.tradeId)

        if (trade.proposer.id != command.userId && trade.receiver.id != command.userId) {
            throw ForbiddenException("Only trade participants can cancel")
        }

        trade.cancel()

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    fun getTrade(tradeId: Long): TradeResponse {
        val trade = tradeRepository.findById(tradeId)
            ?: throw EntityNotFoundException("Trade", tradeId)
        return TradeResponse.from(trade)
    }

    fun getTradesByUser(userId: UUID): List<TradeResponse> {
        return tradeRepository.findByProposerIdOrReceiverId(userId)
            .map { TradeResponse.from(it) }
    }
}
