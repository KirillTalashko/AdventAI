package com.example.core.domain.usecase

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer
import javax.inject.Inject

class SendDetailedChatMessageUseCase @Inject constructor(
    private val repository: AiChatRepository
) {
    suspend operator fun invoke(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer> {
        val trimmedMessage = message.trim()

        return if (trimmedMessage.isBlank()) {
            AppResult.Error(AppError.EmptyPrompt)
        } else {
            repository.sendDetailedMessage(
                message = trimmedMessage,
                options = options
            )
        }
    }
}
