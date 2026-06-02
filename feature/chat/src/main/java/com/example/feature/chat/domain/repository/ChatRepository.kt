package com.example.feature.chat.domain.repository

interface ChatRepository {
    suspend fun sendMessage(message: String): String
}
