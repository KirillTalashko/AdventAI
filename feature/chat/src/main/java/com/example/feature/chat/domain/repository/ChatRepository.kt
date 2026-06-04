package com.example.feature.chat.domain.repository

import com.example.feature.chat.domain.model.ChatRequestOptions

interface ChatRepository {
    suspend fun sendMessage(
        message: String,
        options: ChatRequestOptions = ChatRequestOptions()
    ): String
}
