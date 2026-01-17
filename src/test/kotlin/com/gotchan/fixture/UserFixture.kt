package com.gotchan.fixture

import com.gotchan.domain.user.model.User
import java.math.BigDecimal
import java.util.*

object UserFixture {

    fun createUser(
        id: UUID = UUID.randomUUID(),
        nickname: String = "tester",
        email: String = "test@test.com",
        password: String = "password123",
        trustScore: BigDecimal = BigDecimal("36.5"),
        addressHash: String? = null
    ) = User(
        id = id,
        nickname = nickname,
        email = email,
        password = password,
        trustScore = trustScore,
        addressHash = addressHash
    )

    fun createUserWithEmail(email: String) = createUser(email = email)

    fun createUserWithNickname(nickname: String) = createUser(nickname = nickname)
}
