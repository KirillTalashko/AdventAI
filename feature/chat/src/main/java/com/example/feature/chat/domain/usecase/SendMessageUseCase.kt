package com.example.feature.chat.domain.usecase

import com.example.feature.chat.domain.model.ChatRequestOptions
import com.example.feature.chat.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): String =
        repository.sendMessage(
            message = message.trim(),
            options = options
        )
}
