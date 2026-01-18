package com.gotchan.domain.user.model

import com.gotchan.common.domain.BaseEntity
import jakarta.persistence.*
import java.math.BigDecimal
import java.util.*

@Entity
@Table(name = "users")
class User(
    @Id
    @Column(columnDefinition = "BINARY(16)")
    val id: UUID = UUID.randomUUID(),

    @Column(nullable = false, unique = true, length = 50)
    var nickname: String,

    @Column(nullable = false, unique = true, length = 100)
    val email: String,

    @Column(nullable = false)
    var password: String,

    @Column(nullable = false, precision = 4, scale = 1)
    var trustScore: BigDecimal = BigDecimal("36.5"),

    @Column(length = 500)
    var addressHash: String? = null

) : BaseEntity() {

    fun updateNickname(nickname: String) {
        this.nickname = nickname
    }

    fun updateAddress(addressHash: String) {
        this.addressHash = addressHash
    }

    fun increaseTrustScore(amount: BigDecimal) {
        this.trustScore = (this.trustScore.add(amount)).min(MAX_TRUST_SCORE)
    }

    fun decreaseTrustScore(amount: BigDecimal) {
        this.trustScore = (this.trustScore.subtract(amount)).max(MIN_TRUST_SCORE)
    }

    companion object {
        val MAX_TRUST_SCORE: BigDecimal = BigDecimal("100.0")
        val MIN_TRUST_SCORE: BigDecimal = BigDecimal.ZERO
    }
}
