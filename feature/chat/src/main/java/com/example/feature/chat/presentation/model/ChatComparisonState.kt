package com.example.feature.chat.presentation.model

data class ChatComparisonState(
    val sourcePrompt: String = "",
    val regularResponse: String? = null,
    val structuredResponse: String? = null,
    val lastMode: ResponseMode? = null
)
