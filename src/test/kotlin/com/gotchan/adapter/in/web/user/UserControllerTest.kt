package com.gotchan.adapter.`in`.web.user

import tools.jackson.databind.ObjectMapper
import com.gotchan.adapter.`in`.web.user.dto.SignUpRequest
import com.gotchan.adapter.`in`.web.user.dto.UpdateUserRequest
import com.gotchan.application.user.dto.UserProfileResponse
import com.gotchan.application.user.dto.UserResponse
import com.gotchan.application.user.port.UserUseCase
import com.gotchan.common.exception.DuplicateEntityException
import com.gotchan.common.exception.EntityNotFoundException
import org.junit.jupiter.api.DisplayName
import org.junit.jupiter.api.Nested
import org.junit.jupiter.api.Test
import org.mockito.kotlin.any
import org.mockito.kotlin.given
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest
import org.springframework.http.MediaType
import org.springframework.test.context.bean.override.mockito.MockitoBean
import org.springframework.test.web.servlet.MockMvc
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*
import org.springframework.test.web.servlet.result.MockMvcResultMatchers.*
import java.math.BigDecimal
import java.util.*

@WebMvcTest(UserController::class)
@DisplayName("UserController")
class UserControllerTest {

    @Autowired
    lateinit var mockMvc: MockMvc

    @Autowired
    lateinit var objectMapper: ObjectMapper

    @MockitoBean
    lateinit var userUseCase: UserUseCase

    @Nested
    @DisplayName("POST /api/v1/users/signup")
    inner class SignUpTest {

        @Test
        fun `회원가입 성공시 201을 반환한다`() {
            // Given
            val request = SignUpRequest(
                email = "test@test.com",
                nickname = "tester",
                password = "password123"
            )
            val response = UserResponse(
                id = UUID.randomUUID(),
                nickname = "tester",
                email = "test@test.com",
                trustScore = BigDecimal("36.5")
            )
            given(userUseCase.signUp(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isCreated)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
        }

        @Test
        fun `이메일 형식이 잘못되면 400을 반환한다`() {
            // Given
            val request = SignUpRequest(
                email = "invalid-email",
                nickname = "tester",
                password = "password123"
            )

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isBadRequest)
                .andExpect(jsonPath("$.success").value(false))
        }

        @Test
        fun `이메일이 중복되면 409를 반환한다`() {
            // Given
            val request = SignUpRequest(
                email = "existing@test.com",
                nickname = "tester",
                password = "password123"
            )
            given(userUseCase.signUp(any()))
                .willThrow(DuplicateEntityException("User", "email", "existing@test.com"))

            // When & Then
            mockMvc.perform(
                post("/api/v1/users/signup")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isConflict)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("GET /api/v1/users/{userId}/profile")
    inner class GetProfileTest {

        @Test
        fun `프로필 조회 성공시 200을 반환한다`() {
            // Given
            val userId = UUID.randomUUID()
            val response = UserProfileResponse(
                id = userId,
                nickname = "tester",
                trustScore = BigDecimal("36.5"),
                hasAddress = true
            )
            given(userUseCase.getProfile(userId)).willReturn(response)

            // When & Then
            mockMvc.perform(get("/api/v1/users/$userId/profile"))
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
        }

        @Test
        fun `존재하지 않는 사용자 조회시 404를 반환한다`() {
            // Given
            val userId = UUID.randomUUID()
            given(userUseCase.getProfile(userId))
                .willThrow(EntityNotFoundException("User", userId))

            // When & Then
            mockMvc.perform(get("/api/v1/users/$userId/profile"))
                .andExpect(status().isNotFound)
                .andExpect(jsonPath("$.success").value(false))
        }
    }

    @Nested
    @DisplayName("PATCH /api/v1/users/{userId}")
    inner class UpdateUserTest {

        @Test
        fun `사용자 정보 수정 성공시 200을 반환한다`() {
            // Given
            val userId = UUID.randomUUID()
            val request = UpdateUserRequest(
                nickname = "newNickname",
                address = null
            )
            val response = UserProfileResponse(
                id = userId,
                nickname = "newNickname",
                trustScore = BigDecimal("36.5"),
                hasAddress = false
            )
            given(userUseCase.updateUser(any())).willReturn(response)

            // When & Then
            mockMvc.perform(
                patch("/api/v1/users/$userId")
                    .contentType(MediaType.APPLICATION_JSON)
                    .content(objectMapper.writeValueAsString(request))
            )
                .andExpect(status().isOk)
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.nickname").value("newNickname"))
        }
    }
}
