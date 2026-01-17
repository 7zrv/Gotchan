package com.gotchan.application.user.dto

import com.gotchan.domain.user.model.User
import java.math.BigDecimal
import java.util.UUID

data class UserResponse(
    val id: UUID,
    val nickname: String,
    val email: String,
    val trustScore: BigDecimal
) {
    companion object {
        fun from(user: User) = UserResponse(
            id = user.id,
            nickname = user.nickname,
            email = user.email,
            trustScore = user.trustScore
        )
    }
}

data class UserProfileResponse(
    val id: UUID,
    val nickname: String,
    val trustScore: BigDecimal,
    val hasAddress: Boolean
) {
    companion object {
        fun from(user: User) = UserProfileResponse(
            id = user.id,
            nickname = user.nickname,
            trustScore = user.trustScore,
            hasAddress = user.addressHash != null
        )
    }
}
