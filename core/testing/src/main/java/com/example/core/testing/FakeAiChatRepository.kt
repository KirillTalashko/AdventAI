package com.example.core.testing

import com.example.core.common.AppError
import com.example.core.common.AppResult
import com.example.core.domain.repository.AiChatRepository
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer

class FakeAiChatRepository : AiChatRepository {
    var result: AppResult<LlmAnswer> = AppResult.Success(LlmAnswer(content = "Fake answer"))
        private set

    val receivedMessages = mutableListOf<String>()

    fun returns(answer: LlmAnswer) {
        result = AppResult.Success(answer)
    }

    fun returnsError(error: AppError) {
        result = AppResult.Error(error)
    }

    override suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions
    ): AppResult<String> {
        receivedMessages += message

        return when (val detailedResult = result) {
            is AppResult.Success -> AppResult.Success(detailedResult.data.content)
            is AppResult.Error -> detailedResult
        }
    }

    override suspend fun sendDetailedMessage(
        message: String,
        options: ChatRequestOptions
    ): AppResult<LlmAnswer> {
        receivedMessages += message
        return result
    }
}
