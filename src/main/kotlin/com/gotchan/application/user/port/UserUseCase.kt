package com.gotchan.application.user.port

import com.gotchan.application.user.dto.SignUpCommand
import com.gotchan.application.user.dto.UpdateUserCommand
import com.gotchan.application.user.dto.UserProfileResponse
import com.gotchan.application.user.dto.UserResponse
import java.util.*

interface UserUseCase {
    fun signUp(command: SignUpCommand): UserResponse
    fun getProfile(userId: UUID): UserProfileResponse
    fun getUser(userId: UUID): UserResponse
    fun updateUser(command: UpdateUserCommand): UserProfileResponse
}
