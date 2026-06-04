package com.example.feature.chat.presentation.model

import com.example.feature.chat.domain.model.ChatRequestOptions

data class PreparedChatRequest(
    val sourcePrompt: String,
    val apiPrompt: String,
    val options: ChatRequestOptions
)
