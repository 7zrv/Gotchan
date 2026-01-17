package com.gotchan.common.exception

import org.springframework.http.HttpStatus

enum class ErrorCode(
    val status: HttpStatus,
    val code: String,
    val message: String
) {
    // Common
    INVALID_INPUT(HttpStatus.BAD_REQUEST, "C001", "Invalid input value"),
    ENTITY_NOT_FOUND(HttpStatus.NOT_FOUND, "C002", "Entity not found"),
    DUPLICATE_ENTITY(HttpStatus.CONFLICT, "C003", "Entity already exists"),
    INVALID_STATE(HttpStatus.BAD_REQUEST, "C004", "Invalid state transition"),
    INTERNAL_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C005", "Internal server error"),

    // Auth
    UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A001", "Unauthorized"),
    FORBIDDEN(HttpStatus.FORBIDDEN, "A002", "Access denied"),
    INVALID_TOKEN(HttpStatus.UNAUTHORIZED, "A003", "Invalid token"),
    EXPIRED_TOKEN(HttpStatus.UNAUTHORIZED, "A004", "Token expired"),

    // User
    USER_NOT_FOUND(HttpStatus.NOT_FOUND, "U001", "User not found"),
    DUPLICATE_EMAIL(HttpStatus.CONFLICT, "U002", "Email already exists"),
    DUPLICATE_NICKNAME(HttpStatus.CONFLICT, "U003", "Nickname already exists"),

    // Item
    ITEM_NOT_FOUND(HttpStatus.NOT_FOUND, "I001", "Item not found"),
    ITEM_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "I002", "Item is not available for trade"),

    // Trade
    TRADE_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "Trade not found"),
    INVALID_TRADE_STATUS(HttpStatus.BAD_REQUEST, "T002", "Invalid trade status transition"),
    SELF_TRADE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "T003", "Cannot trade with yourself")
}
