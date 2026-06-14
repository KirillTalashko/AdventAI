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

    /**
     * Создать эфемерный (session-scoped) диалог для демо-заливки контекста. Такие диалоги
     * не попадают в список диалогов и статистику и удаляются в конце сессии.
     */
    suspend fun createEphemeralConversation(agentId: String, title: String): Long

    /** Удалить все эфемерные диалоги агента (очистка в конце сессии / при старте). */
    suspend fun deleteEphemeralConversations(agentId: String)

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

    // --- Сжатие истории (Day 9): summary хранится отдельно от самих сообщений ---

    /** Состояние свёртки диалога: текущий summary и сколько старых сообщений он покрывает. */
    suspend fun getSummaryState(conversationId: Long): ConversationSummaryState

    /** Сохранить обновлённый summary и число свёрнутых сообщений. */
    suspend fun updateSummary(conversationId: Long, summary: String?, summarizedCount: Int)

    /** Сводка по токенам всех диалогов (для экрана «Статистика»), свежие сверху. */
    fun observeTokenStats(): Flow<List<ConversationTokenStat>>

    // --- Банк вопросов для демо-заливки контекста (session-scoped, чистится в конце сессии) ---

    /** Залить банк визовых вопросов, если он ещё пуст (idempotent на сессию). */
    suspend fun seedFillerQuestions(questions: List<String>)

    /** Неиспользованные вопросы по порядку. */
    suspend fun getUnusedFillerQuestions(): List<FillerQuestion>

    /** Пометить вопрос использованным. */
    suspend fun markFillerUsed(id: Long)

    /** Очистить банк вопросов (конец сессии / при старте). */
    suspend fun clearFillerQuestions()
}

/** Вопрос-наполнитель для демо-заливки контекста. */
data class FillerQuestion(
    val id: Long,
    val text: String
)

/** Состояние сжатия истории диалога: краткое содержание и сколько сообщений им покрыто. */
data class ConversationSummaryState(
    val summary: String?,
    val summarizedCount: Int
) {
    companion object {
        val Empty = ConversationSummaryState(summary = null, summarizedCount = 0)
    }
}
