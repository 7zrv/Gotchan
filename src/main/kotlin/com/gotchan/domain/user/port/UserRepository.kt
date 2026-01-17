package com.gotchan.domain.user.port

import com.gotchan.domain.user.model.User
import java.util.*

interface UserRepository {
    fun save(user: User): User
    fun findById(id: UUID): User?
    fun findByEmail(email: String): User?
    fun findByNickname(nickname: String): User?
    fun existsByEmail(email: String): Boolean
    fun existsByNickname(nickname: String): Boolean
    fun delete(user: User)
}
