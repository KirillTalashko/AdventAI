package com.example.core.network.api

import com.example.core.network.dto.ChatRequestDto
import com.example.core.network.dto.ChatResponseDto
import retrofit2.http.Body
import retrofit2.http.POST

interface OpenRouterApi {
    @POST("chat/completions")
    suspend fun sendMessage(
        @Body request: ChatRequestDto
    ): ChatResponseDto
}
