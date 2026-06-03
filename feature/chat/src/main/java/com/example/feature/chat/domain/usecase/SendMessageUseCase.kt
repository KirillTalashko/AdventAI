package com.example.feature.chat.domain.usecase

import com.example.feature.chat.domain.repository.ChatRepository

class SendMessageUseCase(
    private val repository: ChatRepository
) {
    suspend operator fun invoke(message: String): String =
        repository.sendMessage(message.trim())
}
