package com.gotchan.application.user.dto

import java.util.UUID

data class SignUpCommand(
    val email: String,
    val nickname: String,
    val password: String
)

data class UpdateUserCommand(
    val userId: UUID,
    val nickname: String?,
    val addressHash: String?
)
