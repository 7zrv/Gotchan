package com.gotchan.application.trade.service

import com.gotchan.application.trade.dto.*
import com.gotchan.application.trade.port.TradeUseCase
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
) : TradeUseCase {

    @Transactional
    override fun requestTrade(command: RequestTradeCommand): TradeResponse {
        val proposer = userRepository.findById(command.proposerId)
            ?: throw EntityNotFoundException("User", command.proposerId)

        val proposerItem = itemRepository.findById(command.proposerItemId)
            ?: throw EntityNotFoundException("Item", command.proposerItemId)

        if (proposerItem.owner.id != proposer.id) {
            throw ForbiddenException("You are not the owner of this item")
        }

        if (!proposerItem.isAvailable()) {
            throw InvalidStateException("Proposer item is not available for trade")
        }

        val receiverItem = itemRepository.findById(command.receiverItemId)
            ?: throw EntityNotFoundException("Item", command.receiverItemId)

        if (receiverItem.owner.id == proposer.id) {
            throw BusinessException(ErrorCode.SELF_TRADE_NOT_ALLOWED)
        }

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
    override fun respondTrade(command: RespondTradeCommand): TradeResponse {
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
    override fun registerTracking(command: RegisterTrackingCommand): TradeResponse {
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
    override fun confirmTrade(command: ConfirmTradeCommand): TradeResponse {
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
    override fun cancelTrade(command: CancelTradeCommand): TradeResponse {
        val trade = tradeRepository.findById(command.tradeId)
            ?: throw EntityNotFoundException("Trade", command.tradeId)

        if (trade.proposer.id != command.userId && trade.receiver.id != command.userId) {
            throw ForbiddenException("Only trade participants can cancel")
        }

        trade.cancel()

        val savedTrade = tradeRepository.save(trade)
        return TradeResponse.from(savedTrade)
    }

    override fun getTrade(tradeId: Long): TradeResponse {
        val trade = tradeRepository.findById(tradeId)
            ?: throw EntityNotFoundException("Trade", tradeId)
        return TradeResponse.from(trade)
    }

    override fun getTradesByUser(userId: UUID): List<TradeResponse> {
        return tradeRepository.findByProposerIdOrReceiverId(userId)
            .map { TradeResponse.from(it) }
    }
}
