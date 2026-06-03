package com.example.feature.chat.domain.exception

class ChatException(
    message: String,
    cause: Throwable? = null
) : Exception(message, cause)
