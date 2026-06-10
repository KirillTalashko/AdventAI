package com.example.core.domain.usecase

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer
import javax.inject.Inject

class SendOpenRouterChatMessageUseCase @Inject constructor(
    private val repository: AiChatRepository
) {
    suspend operator fun invoke(
        message: String,
        modelId: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer> {
        val trimmedMessage = message.trim()
        val trimmedModelId = modelId.trim()

        return when {
            trimmedMessage.isBlank() -> AppResult.Error(AppError.EmptyPrompt)
            trimmedModelId.isBlank() -> AppResult.Error(AppError.InvalidParameters)
            else -> repository.sendOpenRouterDetailedMessage(
                message = trimmedMessage,
                modelId = trimmedModelId,
                options = options
            )
        }
    }
}
