package com.gotchan.adapter.out.persistence.user

import com.gotchan.domain.user.model.User
import com.gotchan.domain.user.port.UserRepository
import org.springframework.data.repository.findByIdOrNull
import org.springframework.stereotype.Repository
import java.util.*

@Repository
class UserRepositoryAdapter(
    private val userJpaRepository: UserJpaRepository
) : UserRepository {

    override fun save(user: User): User = userJpaRepository.save(user)

    override fun findById(id: UUID): User? = userJpaRepository.findByIdOrNull(id)

    override fun findByEmail(email: String): User? = userJpaRepository.findByEmail(email)

    override fun findByNickname(nickname: String): User? = userJpaRepository.findByNickname(nickname)

    override fun existsByEmail(email: String): Boolean = userJpaRepository.existsByEmail(email)

    override fun existsByNickname(nickname: String): Boolean = userJpaRepository.existsByNickname(nickname)

    override fun delete(user: User) = userJpaRepository.delete(user)
}
