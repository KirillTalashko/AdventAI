package com.example.feature.chat.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.fold
import com.example.core.domain.agent.AiAgent
import com.example.core.domain.repository.ChatHistoryRepository
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.Conversation
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.navigation.ChatDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Job
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.flow.filterNotNull
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

private const val DEFAULT_CONVERSATION_TITLE = "Новый диалог"
private const val TITLE_MAX_LENGTH = 40

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val agent: AiAgent,
    private val chatHistoryRepository: ChatHistoryRepository,
    private val errorMessageMapper: ChatErrorMessageMapper
) : ViewModel() {
    private val agentId: String =
        savedStateHandle[ChatDestination.AGENT_ID_ARGUMENT] ?: ChatDestination.VISA_AGENT_ID

    private val activeConversationId = MutableStateFlow<Long?>(null)

    /** Текущая задача генерации ответа — храним, чтобы её можно было отменить кнопкой «стоп». */
    private var sendJob: Job? = null

    private val _uiState = MutableStateFlow(
        AgentChatUiState(config = defaultConfig(agentId))
    )
    val uiState: StateFlow<AgentChatUiState> = _uiState.asStateFlow()

    init {
        // Открываем последний диалог агента; если их ещё нет — создаём первый.
        viewModelScope.launch {
            val latest = chatHistoryRepository.latestConversationId(agentId)
            activeConversationId.value = latest ?: createConversationWithGreeting()
        }
        // Список диалогов агента для переключения.
        viewModelScope.launch {
            chatHistoryRepository.observeConversations(agentId).collect { conversations ->
                _uiState.update { state -> state.copy(conversations = conversations) }
            }
        }
        // Сообщения активного диалога; при переключении поток пересоздаётся.
        viewModelScope.launch {
            activeConversationId.filterNotNull().collectLatest { conversationId ->
                _uiState.update { state -> state.copy(activeConversationId = conversationId) }
                chatHistoryRepository.observeMessages(conversationId).collect { messages ->
                    _uiState.update { state -> state.copy(messages = messages) }
                }
            }
        }
    }

    fun onMessageChanged(message: String) {
        _uiState.update { state ->
            state.copy(message = message)
        }
    }

    fun sendMessage() {
        val prompt = uiState.value.message.trim()
        if (prompt.isBlank()) {
            _uiState.update { state ->
                state.copy(errorMessage = errorMessageMapper.emptyMessage())
            }
            return
        }

        val conversationId = activeConversationId.value ?: return
        val config = uiState.value.config
        _uiState.update { state ->
            state.copy(message = "", isLoading = true, errorMessage = null)
        }

        sendJob = viewModelScope.launch {
            val isFirstUserMessage = chatHistoryRepository.getMessages(conversationId)
                .none { it.author == AgentMessageAuthor.User }

            chatHistoryRepository.appendMessage(
                conversationId = conversationId,
                message = AgentChatMessage(
                    author = AgentMessageAuthor.User,
                    text = prompt,
                    createdAt = System.currentTimeMillis()
                )
            )
            if (isFirstUserMessage) {
                // Авто-заголовок диалога по первому сообщению пользователя (как в ChatGPT).
                chatHistoryRepository.updateConversationTitle(conversationId, prompt.toConversationTitle())
            }

            val conversation = chatHistoryRepository.getMessages(conversationId)
            agent.ask(config = config, conversation = conversation).fold(
                onSuccess = { answer ->
                    chatHistoryRepository.appendMessage(
                        conversationId = conversationId,
                        message = AgentChatMessage(
                            author = AgentMessageAuthor.Agent,
                            text = answer.content,
                            createdAt = System.currentTimeMillis()
                        )
                    )
                    chatHistoryRepository.touchConversation(conversationId)
                    _uiState.update { state -> state.copy(isLoading = false) }
                },
                onError = { error ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            errorMessage = errorMessageMapper.map(error)
                        )
                    }
                }
            )
        }
    }

    /** Прерывает текущую генерацию (кнопка «стоп» в композере). Запрос отменяется. */
    fun onStopGeneration() {
        sendJob?.cancel()
        sendJob = null
        _uiState.update { state -> state.copy(isLoading = false) }
    }

    fun onNewDialog() {
        viewModelScope.launch {
            activeConversationId.value = createConversationWithGreeting()
        }
    }

    fun onSelectConversation(conversationId: Long) {
        activeConversationId.value = conversationId
    }

    fun onDeleteConversation(conversationId: Long) {
        viewModelScope.launch {
            chatHistoryRepository.deleteConversation(conversationId)
            if (activeConversationId.value == conversationId) {
                activeConversationId.value = chatHistoryRepository.latestConversationId(agentId)
                    ?: createConversationWithGreeting()
            }
        }
    }

    fun onAgentNameChanged(value: String) {
        updateConfig { copy(name = value) }
    }

    fun onModelSelected(model: AgentLlmModel) {
        updateConfig { copy(model = model) }
    }

    fun onSystemPromptChanged(value: String) {
        updateConfig { copy(systemPrompt = value) }
    }

    fun onDialogThemeChanged(value: String) {
        updateConfig { copy(dialogTheme = value) }
    }

    private fun updateConfig(update: AgentConfig.() -> AgentConfig) {
        _uiState.update { state ->
            state.copy(config = state.config.update())
        }
    }

    private suspend fun createConversationWithGreeting(): Long {
        val id = chatHistoryRepository.createConversation(agentId, DEFAULT_CONVERSATION_TITLE)
        chatHistoryRepository.appendMessage(id, greetingMessage())
        return id
    }

    private fun String.toConversationTitle(): String {
        val singleLine = trim().replace("\n", " ")
        return if (singleLine.length <= TITLE_MAX_LENGTH) {
            singleLine
        } else {
            singleLine.take(TITLE_MAX_LENGTH).trimEnd() + "…"
        }
    }

    private fun greetingMessage(): AgentChatMessage =
        AgentChatMessage(
            author = AgentMessageAuthor.Agent,
            text = if (agentId == ChatDestination.NEW_AGENT_ID) {
                "Привет! Я ваш новый AI-агент. Опишите задачу — я помогу."
            } else {
                "Здравствуйте. Я помогу подготовиться к визовому процессу: разберу ситуацию, документы, риски и следующие шаги."
            },
            createdAt = System.currentTimeMillis()
        )

    private fun defaultConfig(agentId: String): AgentConfig =
        if (agentId == ChatDestination.NEW_AGENT_ID) {
            AgentConfig(
                name = "Новый агент",
                model = AgentLlmModel.DeepSeekFlash,
                dialogTheme = "Рабочий помощник",
                systemPrompt = "Ты персональный AI-агент. Сначала уточняй недостающий контекст, затем давай короткий план и конкретный ответ."
            )
        } else {
            AgentConfig(
                name = "Визовый специалист",
                model = AgentLlmModel.DeepSeekFlash,
                dialogTheme = "Консультация по визам, документам и подготовке к подаче",
                systemPrompt = "Ты визовый специалист. Помогай пользователю разобраться с требованиями, документами, сроками, рисками отказа и подготовкой к подаче. Не выдавай юридические гарантии, отмечай, когда нужно проверить правила конкретной страны или обратиться к официальному источнику.\n\nКогда перечисляешь пакет документов, оформляй его отдельным блоком ровно в таком формате (одна строка — один документ, статус через точку с запятой, статусы только: нужен / загружен / проверен):\n[checklist]\n- Загранпаспорт; нужен\n- Фото 35×45; нужен\n[/checklist]\nОстальной текст пиши обычным образом до или после блока."
            )
        }
}

data class AgentChatUiState(
    val config: AgentConfig,
    val message: String = "",
    val messages: List<AgentChatMessage> = emptyList(),
    val conversations: List<Conversation> = emptyList(),
    val activeConversationId: Long? = null,
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableModels: List<AgentLlmModel> = AgentLlmModel.entries
)
