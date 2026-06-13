package com.example.core.network.dto

import com.google.gson.annotations.SerializedName

data class ChatRequestDto(
    @SerializedName("model")
    val model: String,
    @SerializedName("messages")
    val messages: List<MessageDto>,
    @SerializedName("thinking")
    val thinking: ThinkingDto? = null,
    @SerializedName("temperature")
    val temperature: Double? = null,
    @SerializedName("top_p")
    val topP: Double? = null,
    @SerializedName("max_tokens")
    val maxTokens: Int? = null,
    @SerializedName("stop")
    val stop: List<String>? = null
)
