package com.example.core.network.dto

import com.google.gson.annotations.SerializedName

data class ChoiceDto(
    @SerializedName("finish_reason")
    val finishReason: String?,
    @SerializedName("message")
    val message: ResponseMessageDto?
)
