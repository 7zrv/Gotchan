package com.gotchan.application.user.service

import com.gotchan.application.user.dto.SignUpCommand
import com.gotchan.application.user.dto.UpdateUserCommand
import com.gotchan.application.user.dto.UserProfileResponse
import com.gotchan.application.user.dto.UserResponse
import com.gotchan.application.user.port.UserUseCase
import com.gotchan.common.exception.DuplicateEntityException
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.domain.user.model.User
import com.gotchan.domain.user.port.UserRepository
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import java.util.*

@Service
@Transactional(readOnly = true)
class UserService(
    private val userRepository: UserRepository
) : UserUseCase {

    @Transactional
    override fun signUp(command: SignUpCommand): UserResponse {
        if (userRepository.existsByEmail(command.email)) {
            throw DuplicateEntityException("User", "email", command.email)
        }
        if (userRepository.existsByNickname(command.nickname)) {
            throw DuplicateEntityException("User", "nickname", command.nickname)
        }

        val user = User(
            email = command.email,
            nickname = command.nickname,
            password = command.password // TODO: 암호화 필요
        )

        val savedUser = userRepository.save(user)
        return UserResponse.from(savedUser)
    }

    override fun getProfile(userId: UUID): UserProfileResponse {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)
        return UserProfileResponse.from(user)
    }

    override fun getUser(userId: UUID): UserResponse {
        val user = userRepository.findById(userId)
            ?: throw EntityNotFoundException("User", userId)
        return UserResponse.from(user)
    }

    @Transactional
    override fun updateUser(command: UpdateUserCommand): UserProfileResponse {
        val user = userRepository.findById(command.userId)
            ?: throw EntityNotFoundException("User", command.userId)

        command.nickname?.let { newNickname ->
            if (newNickname != user.nickname && userRepository.existsByNickname(newNickname)) {
                throw DuplicateEntityException("User", "nickname", newNickname)
            }
            user.updateNickname(newNickname)
        }

        command.addressHash?.let { newAddress ->
            user.updateAddress(newAddress)
        }

        val savedUser = userRepository.save(user)
        return UserProfileResponse.from(savedUser)
    }
}
