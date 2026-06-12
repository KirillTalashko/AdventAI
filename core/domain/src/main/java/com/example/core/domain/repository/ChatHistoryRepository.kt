package com.example.core.domain.repository

import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.Conversation
import com.example.core.model.ai.ConversationTokenStat
import kotlinx.coroutines.flow.Flow

/**
 * Хранилище диалогов и их сообщений (Room). У одного агента может быть несколько
 * диалогов («тем»); сообщения принадлежат конкретному диалогу.
 */
interface ChatHistoryRepository {
    /** Диалоги агента, свежие сверху. */
    fun observeConversations(agentId: String): Flow<List<Conversation>>

    /** id самого свежего диалога агента (или null, если диалогов ещё нет). */
    suspend fun latestConversationId(agentId: String): Long?

    /** Создать новый диалог, вернуть его id. */
    suspend fun createConversation(agentId: String, title: String): Long

    suspend fun updateConversationTitle(conversationId: Long, title: String)

    /** Поднять диалог наверх списка (обновить updated_at). */
    suspend fun touchConversation(conversationId: Long)

    /** Удалить диалог вместе с его сообщениями. */
    suspend fun deleteConversation(conversationId: Long)

    /** Реактивный поток сообщений диалога (для UI). */
    fun observeMessages(conversationId: Long): Flow<List<AgentChatMessage>>

    /** Снимок сообщений диалога (для сборки запроса к LLM). */
    suspend fun getMessages(conversationId: Long): List<AgentChatMessage>

    /** Добавить сообщение в диалог. */
    suspend fun appendMessage(conversationId: Long, message: AgentChatMessage)

    /** Сводка по токенам всех диалогов (для экрана «Статистика»), свежие сверху. */
    fun observeTokenStats(): Flow<List<ConversationTokenStat>>
}
