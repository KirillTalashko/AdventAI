package com.example.feature.chat.presentation.temperature.runner

import com.example.core.common.fold
import com.example.core.domain.usecase.SendChatMessageUseCase
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.ThinkingMode
import com.example.feature.chat.R
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.temperature.mapper.TemperaturePromptFactory
import com.example.feature.chat.presentation.temperature.model.TemperatureMode
import com.example.feature.chat.presentation.temperature.model.TemperatureResult
import com.example.feature.chat.presentation.temperature.model.TemperatureUiState
import com.example.feature.chat.presentation.text.ChatTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class TemperatureExperimentRunner @Inject constructor(
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val promptFactory: TemperaturePromptFactory,
    private val errorMessageMapper: ChatErrorMessageMapper,
    private val textProvider: ChatTextProvider
) {
    fun run(prompt: String): Flow<TemperatureUiState> = flow {
        val sourcePrompt = prompt.trim()
        var state = TemperatureUiState(
            prompt = sourcePrompt,
            results = TemperatureMode.entries.associateWith(::initialResult),
            isLoadingAnswers = true
        )

        emit(state)

        suspend fun updateResult(
            mode: TemperatureMode,
            transform: (TemperatureResult) -> TemperatureResult
        ) {
            state = state.copy(
                results = state.results.toMutableMap().apply {
                    put(mode, transform(getValue(mode)))
                }
            )
            emit(state)
        }

        TemperatureMode.entries.forEach { mode ->
            updateResult(mode) { result ->
                result.copy(isLoading = true, answer = "", error = null)
            }

            sendChatMessageUseCase(
                message = sourcePrompt,
                options = ChatRequestOptions(
                    temperature = mode.value,
                    maxTokens = ANSWER_MAX_TOKENS,
                    thinkingMode = ThinkingMode.Disabled
                )
            ).fold(
                onSuccess = { answer ->
                    updateResult(mode) { result ->
                        result.copy(
                            answer = answer,
                            isLoading = false
                        )
                    }
                },
                onError = { error ->
                    updateResult(mode) { result ->
                        result.copy(
                            isLoading = false,
                            error = errorMessageMapper.map(error)
                        )
                    }
                }
            )
        }

        state = state.copy(
            isLoadingAnswers = false,
            isComparing = true
        )
        emit(state)

        val receivedResults = state.results.filterValues { result ->
            result.answer.isNotBlank()
        }

        if (receivedResults.isEmpty()) {
            emit(
                state.copy(
                    isComparing = false,
                    error = textProvider.get(R.string.error_no_temperature_answers)
                )
            )
            return@flow
        }

        sendChatMessageUseCase(
            message = promptFactory.comparisonPrompt(
                prompt = sourcePrompt,
                results = receivedResults
            ),
            options = ChatRequestOptions(
                temperature = COMPARISON_TEMPERATURE,
                maxTokens = COMPARISON_MAX_TOKENS,
                thinkingMode = ThinkingMode.Disabled
            )
        ).fold(
            onSuccess = { conclusion ->
                emit(
                    state.copy(
                        conclusion = conclusion,
                        isComparing = false,
                        error = null
                    )
                )
            },
            onError = { error ->
                emit(
                    state.copy(
                        isComparing = false,
                        error = errorMessageMapper.map(error)
                    )
                )
            }
        )
    }

    private fun initialResult(mode: TemperatureMode): TemperatureResult =
        TemperatureResult(
            mode = mode,
            title = promptFactory.title(mode),
            label = promptFactory.label(mode)
        )

    private companion object {
        const val ANSWER_MAX_TOKENS = 700
        const val COMPARISON_MAX_TOKENS = 900
        const val COMPARISON_TEMPERATURE = 0.0
    }
}
