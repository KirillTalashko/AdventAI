package com.example.feature.chat.presentation.reasoning.model

data class ReasoningResult(
    val mode: ReasoningMode,
    val title: String,
    val prompt: String,
    val answer: String,
    val reasoningContent: String? = null,
    val isLoading: Boolean = false,
    val error: String? = null
)
