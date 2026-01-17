package com.gotchan.adapter.out.persistence.user

import com.gotchan.domain.user.model.User
import org.springframework.data.jpa.repository.JpaRepository
import java.util.*

interface UserJpaRepository : JpaRepository<User, UUID> {
    fun findByEmail(email: String): User?
    fun findByNickname(nickname: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
}
