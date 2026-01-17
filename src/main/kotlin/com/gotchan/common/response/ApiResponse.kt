package com.gotchan.common.response

import com.fasterxml.jackson.annotation.JsonInclude
import java.time.LocalDateTime

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ApiResponse<T>(
    val success: Boolean,
    val data: T? = null,
    val error: ErrorDetail? = null,
    val timestamp: LocalDateTime = LocalDateTime.now()
) {
    companion object {
        fun <T> success(data: T): ApiResponse<T> {
            return ApiResponse(success = true, data = data)
        }

        fun <T> success(): ApiResponse<T> {
            return ApiResponse(success = true)
        }

        fun <T> error(code: String, message: String): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorDetail(code = code, message = message)
            )
        }

        fun <T> error(code: String, message: String, details: Map<String, String>?): ApiResponse<T> {
            return ApiResponse(
                success = false,
                error = ErrorDetail(code = code, message = message, details = details)
            )
        }
    }
}

@JsonInclude(JsonInclude.Include.NON_NULL)
data class ErrorDetail(
    val code: String,
    val message: String,
    val details: Map<String, String>? = null
)
