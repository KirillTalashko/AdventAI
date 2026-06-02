package com.example.core.network.dto

import com.google.gson.annotations.SerializedName

data class ResponseMessageDto(
    @SerializedName("role")
    val role: String?,
    @SerializedName("content")
    val content: String?
)
