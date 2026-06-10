package com.example.feature.chat.presentation.viewmodel

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.common.fold
import com.example.core.domain.agent.AiAgent
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.navigation.ChatDestination
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class ChatViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val agent: AiAgent,
    private val errorMessageMapper: ChatErrorMessageMapper
) : ViewModel() {
    private val initialAgentId: String =
        savedStateHandle[ChatDestination.AGENT_ID_ARGUMENT] ?: ChatDestination.VISA_AGENT_ID

    private val _uiState = MutableStateFlow(
        AgentChatUiState(
            config = defaultConfig(initialAgentId),
            messages = listOf(
                AgentChatMessage(
                    author = AgentMessageAuthor.Agent,
                    text = "Здравствуйте. Я помогу подготовиться к визовому процессу: разберу ситуацию, документы, риски и следующие шаги."
                )
            )
        )
    )
    val uiState: StateFlow<AgentChatUiState> = _uiState.asStateFlow()

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

        val config = uiState.value.config
        _uiState.update { state ->
            state.copy(
                message = "",
                isLoading = true,
                errorMessage = null,
                messages = state.messages + AgentChatMessage(
                    author = AgentMessageAuthor.User,
                    text = prompt
                )
            )
        }

        viewModelScope.launch {
            agent.ask(config = config, userRequest = prompt).fold(
                onSuccess = { answer ->
                    _uiState.update { state ->
                        state.copy(
                            isLoading = false,
                            messages = state.messages + AgentChatMessage(
                                author = AgentMessageAuthor.Agent,
                                text = answer.content
                            )
                        )
                    }
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
                systemPrompt = "Ты визовый специалист. Помогай пользователю разобраться с требованиями, документами, сроками, рисками отказа и подготовкой к подаче. Не выдавай юридические гарантии, отмечай, когда нужно проверить правила конкретной страны или обратиться к официальному источнику."
            )
        }
}

data class AgentChatUiState(
    val config: AgentConfig,
    val message: String = "",
    val messages: List<AgentChatMessage> = emptyList(),
    val isLoading: Boolean = false,
    val errorMessage: String? = null,
    val availableModels: List<AgentLlmModel> = AgentLlmModel.entries
)
