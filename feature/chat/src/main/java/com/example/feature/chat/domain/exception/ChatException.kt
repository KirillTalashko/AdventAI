package com.example.feature.chat.domain.exception

sealed class ChatException(
    cause: Throwable? = null
) : Exception(cause) {
    class EmptyResponse : ChatException()

    class TokenLimitReached : ChatException()

    class UnknownHost(cause: Throwable) : ChatException(cause)

    class Timeout(cause: Throwable) : ChatException(cause)

    class Connection(cause: Throwable) : ChatException(cause)

    class SecureConnection(
        val details: String,
        cause: Throwable
    ) : ChatException(cause)

    class Network(
        val details: String,
        cause: Throwable
    ) : ChatException(cause)

    class Http(
        val code: Int,
        val statusMessage: String?,
        val details: String?,
        cause: Throwable
    ) : ChatException(cause)

    class Unexpected(
        val details: String?,
        cause: Throwable
    ) : ChatException(cause)
}
