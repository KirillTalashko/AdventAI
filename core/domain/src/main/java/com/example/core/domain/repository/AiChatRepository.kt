package com.example.core.domain.repository

import com.example.core.common.AppResult
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.LlmAnswer

interface AiChatRepository {
    suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<String>

    suspend fun sendDetailedMessage(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): AppResult<LlmAnswer>
}
