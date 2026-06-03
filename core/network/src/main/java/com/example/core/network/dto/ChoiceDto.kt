package com.example.core.network.dto

import com.google.gson.annotations.SerializedName

data class ChoiceDto(
    @SerializedName("message")
    val message: ResponseMessageDto?
)
