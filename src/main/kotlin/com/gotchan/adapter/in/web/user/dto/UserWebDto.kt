package com.gotchan.adapter.`in`.web.user.dto

import com.gotchan.application.user.dto.SignUpCommand
import com.gotchan.application.user.dto.UpdateUserCommand
import jakarta.validation.constraints.Email
import jakarta.validation.constraints.NotBlank
import jakarta.validation.constraints.Size
import java.util.*

data class SignUpRequest(
    @field:NotBlank(message = "이메일은 필수입니다")
    @field:Email(message = "올바른 이메일 형식이 아닙니다")
    val email: String,

    @field:NotBlank(message = "닉네임은 필수입니다")
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
    val nickname: String,

    @field:NotBlank(message = "비밀번호는 필수입니다")
    @field:Size(min = 8, max = 50, message = "비밀번호는 8~50자 사이여야 합니다")
    val password: String
) {
    fun toCommand() = SignUpCommand(
        email = email,
        nickname = nickname,
        password = password
    )
}

data class UpdateUserRequest(
    @field:Size(min = 2, max = 20, message = "닉네임은 2~20자 사이여야 합니다")
    val nickname: String?,

    val address: String?
) {
    fun toCommand(userId: UUID) = UpdateUserCommand(
        userId = userId,
        nickname = nickname,
        addressHash = address // TODO: 실제로는 암호화 필요
    )
}
