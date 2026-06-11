package com.example.core.data.ai.repository

import com.example.core.common.DefaultDispatcherProvider
import com.example.core.common.DispatcherProvider
import com.example.core.data.ai.local.ChatMessageDao
import com.example.core.data.ai.local.ConversationDao
import com.example.core.data.ai.local.ConversationEntity
import com.example.core.data.ai.mapper.toDomain
import com.example.core.data.ai.mapper.toEntity
import com.example.core.domain.repository.ChatHistoryRepository
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.Conversation
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.withContext
import javax.inject.Inject

class ChatHistoryRepositoryImpl @Inject constructor(
    private val conversationDao: ConversationDao,
    private val messageDao: ChatMessageDao,
    private val dispatchers: DispatcherProvider = DefaultDispatcherProvider
) : ChatHistoryRepository {
    override fun observeConversations(agentId: String): Flow<List<Conversation>> =
        conversationDao.observeByAgent(agentId).map { list ->
            list.map { it.toDomain() }
        }

    override suspend fun latestConversationId(agentId: String): Long? =
        withContext(dispatchers.io) {
            conversationDao.latestIdForAgent(agentId)
        }

    override suspend fun createConversation(agentId: String, title: String): Long =
        withContext(dispatchers.io) {
            val now = System.currentTimeMillis()
            conversationDao.insert(
                ConversationEntity(
                    agentId = agentId,
                    title = title,
                    createdAt = now,
                    updatedAt = now
                )
            )
        }

    override suspend fun updateConversationTitle(conversationId: Long, title: String) {
        withContext(dispatchers.io) {
            conversationDao.updateTitle(conversationId, title)
        }
    }

    override suspend fun touchConversation(conversationId: Long) {
        withContext(dispatchers.io) {
            conversationDao.touch(conversationId, System.currentTimeMillis())
        }
    }

    override suspend fun deleteConversation(conversationId: Long) {
        withContext(dispatchers.io) {
            conversationDao.delete(conversationId)
        }
    }

    override fun observeMessages(conversationId: Long): Flow<List<AgentChatMessage>> =
        messageDao.observeByConversation(conversationId).map { entities ->
            entities.map { it.toDomain() }
        }

    override suspend fun getMessages(conversationId: Long): List<AgentChatMessage> =
        withContext(dispatchers.io) {
            messageDao.getByConversation(conversationId).map { it.toDomain() }
        }

    override suspend fun appendMessage(conversationId: Long, message: AgentChatMessage) {
        withContext(dispatchers.io) {
            messageDao.insert(message.toEntity(conversationId = conversationId))
        }
    }
}
