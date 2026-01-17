package com.gotchan.common.exception

open class BusinessException(
    val errorCode: ErrorCode,
    override val message: String = errorCode.message
) : RuntimeException(message)

class EntityNotFoundException(
    entityName: String,
    id: Any
) : BusinessException(
    errorCode = ErrorCode.ENTITY_NOT_FOUND,
    message = "$entityName not found with id: $id"
)

class DuplicateEntityException(
    entityName: String,
    field: String,
    value: Any
) : BusinessException(
    errorCode = ErrorCode.DUPLICATE_ENTITY,
    message = "$entityName already exists with $field: $value"
)

class InvalidStateException(
    message: String
) : BusinessException(
    errorCode = ErrorCode.INVALID_STATE,
    message = message
)

class UnauthorizedException(
    message: String = "Unauthorized access"
) : BusinessException(
    errorCode = ErrorCode.UNAUTHORIZED,
    message = message
)

class ForbiddenException(
    message: String = "Access denied"
) : BusinessException(
    errorCode = ErrorCode.FORBIDDEN,
    message = message
)
