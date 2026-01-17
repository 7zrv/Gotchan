package com.gotchan.application.user.service

import com.gotchan.application.user.dto.SignUpCommand
import com.gotchan.application.user.dto.UpdateUserCommand
import com.gotchan.common.exception.DuplicateEntityException
import com.gotchan.common.exception.EntityNotFoundException
import com.gotchan.domain.user.model.User
import com.gotchan.domain.user.port.UserRepository
import com.gotchan.fixture.UserFixture
import org.assertj.core.api.Assertions.assertThat
import org.junit.jupiter.api.BeforeEach
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.junit.jupiter.api.extension.ExtendWith
import org.mockito.Mock
import org.mockito.junit.jupiter.MockitoExtension
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.mockito.kotlin.verify
import java.util.*

@ExtendWith(MockitoExtension::class)
@DisplayName("UserService")
class UserServiceTest {

    @Mock
    lateinit var userRepository: UserRepository

    lateinit var userService: UserService

    @BeforeEach
    fun setUp() {
        userService = UserService(userRepository)
    }

    @Nested
    @DisplayName("회원가입")
    inner class SignUpTest {

        @Test
        fun `정상적으로 회원가입할 수 있다`() {
            // Given
            val command = SignUpCommand(
                email = "test@test.com",
                nickname = "tester",
                password = "password123"
            )
            given(userRepository.existsByEmail(command.email)).willReturn(false)
            given(userRepository.existsByNickname(command.nickname)).willReturn(false)
            given(userRepository.save(any<User>())).willAnswer { it.arguments[0] as User }

            // When
            val result = userService.signUp(command)

            // Then
            assertThat(result.email).isEqualTo(command.email)
            assertThat(result.nickname).isEqualTo(command.nickname)
            verify(userRepository).save(any<User>())
        }

        @Test
        fun `이메일이 중복되면 회원가입에 실패한다`() {
            // Given
            val command = SignUpCommand(
                email = "existing@test.com",
                nickname = "tester",
                password = "password123"
            )
            given(userRepository.existsByEmail(command.email)).willReturn(true)

            // When & Then
            assertThrows<DuplicateEntityException> {
                userService.signUp(command)
            }
        }

        @Test
        fun `닉네임이 중복되면 회원가입에 실패한다`() {
            // Given
            val command = SignUpCommand(
                email = "test@test.com",
                nickname = "existingNickname",
                password = "password123"
            )
            given(userRepository.existsByEmail(command.email)).willReturn(false)
            given(userRepository.existsByNickname(command.nickname)).willReturn(true)

            // When & Then
            assertThrows<DuplicateEntityException> {
                userService.signUp(command)
            }
        }
    }

    @Nested
    @DisplayName("프로필 조회")
    inner class GetProfileTest {

        @Test
        fun `사용자 프로필을 조회할 수 있다`() {
            // Given
            val userId = UUID.randomUUID()
            val user = UserFixture.createUser(id = userId, nickname = "tester")
            given(userRepository.findById(userId)).willReturn(user)

            // When
            val result = userService.getProfile(userId)

            // Then
            assertThat(result.id).isEqualTo(userId)
            assertThat(result.nickname).isEqualTo("tester")
        }

        @Test
        fun `존재하지 않는 사용자 프로필 조회시 실패한다`() {
            // Given
            val userId = UUID.randomUUID()
            given(userRepository.findById(userId)).willReturn(null)

            // When & Then
            assertThrows<EntityNotFoundException> {
                userService.getProfile(userId)
            }
        }
    }

    @Nested
    @DisplayName("정보 수정")
    inner class UpdateUserTest {

        @Test
        fun `닉네임을 변경할 수 있다`() {
            // Given
            val userId = UUID.randomUUID()
            val user = UserFixture.createUser(id = userId, nickname = "oldNickname")
            val command = UpdateUserCommand(
                userId = userId,
                nickname = "newNickname",
                addressHash = null
            )
            given(userRepository.findById(userId)).willReturn(user)
            given(userRepository.existsByNickname("newNickname")).willReturn(false)
            given(userRepository.save(any<User>())).willAnswer { it.arguments[0] as User }

            // When
            val result = userService.updateUser(command)

            // Then
            assertThat(result.nickname).isEqualTo("newNickname")
        }

        @Test
        fun `변경하려는 닉네임이 이미 존재하면 실패한다`() {
            // Given
            val userId = UUID.randomUUID()
            val user = UserFixture.createUser(id = userId, nickname = "oldNickname")
            val command = UpdateUserCommand(
                userId = userId,
                nickname = "existingNickname",
                addressHash = null
            )
            given(userRepository.findById(userId)).willReturn(user)
            given(userRepository.existsByNickname("existingNickname")).willReturn(true)

            // When & Then
            assertThrows<DuplicateEntityException> {
                userService.updateUser(command)
            }
        }

        @Test
        fun `배송지 주소를 변경할 수 있다`() {
            // Given
            val userId = UUID.randomUUID()
            val user = UserFixture.createUser(id = userId)
            val command = UpdateUserCommand(
                userId = userId,
                nickname = null,
                addressHash = "newEncryptedAddress"
            )
            given(userRepository.findById(userId)).willReturn(user)
            given(userRepository.save(any<User>())).willAnswer { it.arguments[0] as User }

            // When
            val result = userService.updateUser(command)

            // Then
            assertThat(result.hasAddress).isTrue()
        }
    }

    @Nested
    @DisplayName("사용자 조회")
    inner class GetUserTest {

        @Test
        fun `ID로 사용자를 조회할 수 있다`() {
            // Given
            val userId = UUID.randomUUID()
            val user = UserFixture.createUser(id = userId)
            given(userRepository.findById(userId)).willReturn(user)

            // When
            val result = userService.getUser(userId)

            // Then
            assertThat(result.id).isEqualTo(userId)
        }

        @Test
        fun `존재하지 않는 사용자 조회시 실패한다`() {
            // Given
            val userId = UUID.randomUUID()
            given(userRepository.findById(userId)).willReturn(null)

            // When & Then
            assertThrows<EntityNotFoundException> {
                userService.getUser(userId)
            }
        }
    }
}
