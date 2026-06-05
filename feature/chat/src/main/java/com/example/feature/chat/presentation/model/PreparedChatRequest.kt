package com.example.feature.chat.presentation.model

import com.example.core.model.ai.ChatRequestOptions

data class PreparedChatRequest(
    val sourcePrompt: String,
    val apiPrompt: String,
    val options: ChatRequestOptions
)
