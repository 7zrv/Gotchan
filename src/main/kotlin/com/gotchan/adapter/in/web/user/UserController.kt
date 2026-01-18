package com.gotchan.adapter.`in`.web.user

import com.gotchan.adapter.`in`.web.user.dto.SignUpRequest
import com.gotchan.adapter.`in`.web.user.dto.UpdateUserRequest
import com.gotchan.application.user.dto.UserProfileResponse
import com.gotchan.application.user.dto.UserResponse
import com.gotchan.application.user.port.UserUseCase
import com.gotchan.common.response.ApiResponse
import jakarta.validation.Valid
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import java.util.*

@RestController
@RequestMapping("/api/v1/users")
class UserController(
    private val userUseCase: UserUseCase
) {

    @PostMapping("/signup")
    fun signUp(@Valid @RequestBody request: SignUpRequest): ResponseEntity<ApiResponse<UserResponse>> {
        val response = userUseCase.signUp(request.toCommand())
        return ResponseEntity.status(HttpStatus.CREATED)
            .body(ApiResponse.success(response))
    }

    @GetMapping("/{userId}/profile")
    fun getProfile(@PathVariable userId: UUID): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val response = userUseCase.getProfile(userId)
        return ResponseEntity.ok(ApiResponse.success(response))
    }

    @PatchMapping("/{userId}")
    fun updateUser(
        @PathVariable userId: UUID,
        @Valid @RequestBody request: UpdateUserRequest
    ): ResponseEntity<ApiResponse<UserProfileResponse>> {
        val response = userUseCase.updateUser(request.toCommand(userId))
        return ResponseEntity.ok(ApiResponse.success(response))
    }
}
