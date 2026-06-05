package com.example.core.model.ai

data class LlmAnswer(
    val content: String,
    val reasoningContent: String? = null
)
