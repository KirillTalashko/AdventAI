package com.example.core.common

sealed interface AppError {
    data object EmptyPrompt : AppError
    data object EmptyResponse : AppError
    data object TokenLimitReached : AppError

    /** Контекст (system prompt + история + запрос) не помещается в окно модели. */
    data object ContextOverflow : AppError
    data object Network : AppError
    data object UnknownHost : AppError
    data object Timeout : AppError
    data object Connection : AppError
    data object Unauthorized : AppError
    data object RateLimit : AppError
    data object InvalidRequest : AppError
    data object InvalidParameters : AppError
    data object InsufficientBalance : AppError
    data object ServerOverloaded : AppError
    data object Server : AppError
    data class SecureConnection(val details: String) : AppError
    data class NetworkDetails(val details: String) : AppError
    data class ServerCode(val code: Int, val message: String?, val details: String?) : AppError
    data class Unknown(val message: String?) : AppError
}
