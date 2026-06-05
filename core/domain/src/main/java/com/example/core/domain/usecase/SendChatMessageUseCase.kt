package com.example.core.domain.usecase

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.ChatRequestOptions
import javax.inject.Inject

class SendChatMessageUseCase @Inject constructor(
    private val repository: AiChatRepository
) {
    suspend operator fun invoke(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<String> {
        val trimmedMessage = message.trim()

        return if (trimmedMessage.isBlank()) {
            AppResult.Error(AppError.EmptyPrompt)
        } else {
            repository.sendMessage(
                message = trimmedMessage,
                options = options
            )
        }
    }
}
