package com.gotchan.domain.trade.model

import com.gotchan.common.domain.BaseEntity
import com.gotchan.common.exception.InvalidStateException
import com.gotchan.domain.item.model.GachaItem
import com.gotchan.domain.user.model.User
import jakarta.persistence.*

@Entity
@Table(name = "trades")
class Trade(
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    val id: Long = 0,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_id", nullable = false)
    val proposer: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_id", nullable = false)
    val receiver: User,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "proposer_item_id", nullable = false)
    val proposerItem: GachaItem,

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "receiver_item_id", nullable = false)
    val receiverItem: GachaItem,

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    var status: TradeStatus = TradeStatus.PENDING,

    @Column(length = 50)
    var proposerTrackingNumber: String? = null,

    @Column(length = 50)
    var receiverTrackingNumber: String? = null,

    @Column(nullable = false)
    var proposerConfirmed: Boolean = false,

    @Column(nullable = false)
    var receiverConfirmed: Boolean = false

) : BaseEntity() {

    fun accept() {
        validateStatusTransition(TradeStatus.PENDING, TradeStatus.ACCEPTED)
        status = TradeStatus.ACCEPTED
        proposerItem.startTrading()
        receiverItem.startTrading()
    }

    fun reject() {
        validateStatusTransition(TradeStatus.PENDING, TradeStatus.CANCELLED)
        status = TradeStatus.CANCELLED
    }

    fun startShipping() {
        validateStatusTransition(TradeStatus.ACCEPTED, TradeStatus.SHIPPING)
        status = TradeStatus.SHIPPING
    }

    fun registerProposerTracking(trackingNumber: String) {
        validateTrackingRegistration()
        this.proposerTrackingNumber = trackingNumber
        if (status == TradeStatus.ACCEPTED) {
            startShipping()
        }
    }

    fun registerReceiverTracking(trackingNumber: String) {
        validateTrackingRegistration()
        this.receiverTrackingNumber = trackingNumber
        if (status == TradeStatus.ACCEPTED) {
            startShipping()
        }
    }

    fun confirmByProposer() {
        validateConfirmation()
        proposerConfirmed = true
        checkAndFinish()
    }

    fun confirmByReceiver() {
        validateConfirmation()
        receiverConfirmed = true
        checkAndFinish()
    }

    private fun validateTrackingRegistration() {
        if (status != TradeStatus.ACCEPTED && status != TradeStatus.SHIPPING) {
            throw InvalidStateException("Cannot register tracking number in current status")
        }
    }

    private fun validateConfirmation() {
        if (status != TradeStatus.SHIPPING) {
            throw InvalidStateException("Cannot confirm in current status")
        }
    }

    private fun checkAndFinish() {
        if (proposerConfirmed && receiverConfirmed) {
            status = TradeStatus.FINISHED
            proposerItem.completeTrading()
            receiverItem.completeTrading()
        }
    }

    fun cancel() {
        if (status == TradeStatus.FINISHED || status == TradeStatus.CANCELLED) {
            throw InvalidStateException("Cannot cancel trade in current status")
        }
        if (status == TradeStatus.ACCEPTED || status == TradeStatus.SHIPPING) {
            proposerItem.cancelTrading()
            receiverItem.cancelTrading()
        }
        status = TradeStatus.CANCELLED
    }

    private fun validateStatusTransition(from: TradeStatus, to: TradeStatus) {
        if (status != from) {
            throw InvalidStateException("Cannot transition from $status to $to")
        }
    }
}

enum class TradeStatus {
    PENDING,
    ACCEPTED,
    SHIPPING,
    FINISHED,
    CANCELLED
}
