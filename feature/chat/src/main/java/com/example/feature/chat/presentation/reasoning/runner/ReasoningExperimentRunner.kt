package com.example.feature.chat.presentation.reasoning.runner

import com.example.core.common.fold
import com.example.core.domain.usecase.SendDetailedChatMessageUseCase
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.DeepSeekModel
import com.example.core.model.ai.ThinkingMode
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.reasoning.mapper.ReasoningPromptFactory
import com.example.feature.chat.presentation.reasoning.model.ReasoningMode
import com.example.feature.chat.presentation.reasoning.model.ReasoningResult
import com.example.feature.chat.presentation.reasoning.model.ReasoningUiState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ReasoningExperimentRunner @Inject constructor(
    private val sendDetailedChatMessageUseCase: SendDetailedChatMessageUseCase,
    private val promptFactory: ReasoningPromptFactory,
    private val errorMessageMapper: ChatErrorMessageMapper
) {
    fun runAnswers(
        task: String,
        selectedModes: Set<ReasoningMode>,
        isDeepReasoningEnabled: Boolean
    ): Flow<ReasoningUiState> = flow {
        val sourceTask = task.trim()
        val orderedModes = selectedModes.ordered()
        var state = ReasoningUiState(
            task = sourceTask,
            selectedModes = selectedModes,
            results = orderedModes.associateWith { mode ->
                initialResult(task = sourceTask, mode = mode)
            },
            isDeepReasoningEnabled = isDeepReasoningEnabled,
            isLoadingAnswers = true
        )

        emit(state)

        suspend fun updateResult(
            mode: ReasoningMode,
            transform: (ReasoningResult) -> ReasoningResult
        ) {
            state = state.copy(
                results = state.results.toMutableMap().apply {
                    val currentResult = getValue(mode)
                    put(mode, transform(currentResult))
                }
            )
            emit(state)
        }

        orderedModes.forEach { mode ->
            executeMode(
                task = sourceTask,
                mode = mode,
                isDeepReasoningEnabled = isDeepReasoningEnabled,
                updateResult = ::updateResult
            )
        }

        state = state.copy(isLoadingAnswers = false)
        emit(state)
    }

    suspend fun compareResults(
        task: String,
        results: Map<ReasoningMode, ReasoningResult>,
        isDeepReasoningEnabled: Boolean
    ): String =
        sendDetailedChatMessageUseCase(
            message = promptFactory.comparisonPrompt(
                task = task,
                results = results.filterValues { result ->
                    result.answer.isNotBlank()
                }
            ),
            options = requestOptions(
                maxTokens = COMPARISON_MAX_TOKENS,
                isDeepReasoningEnabled = isDeepReasoningEnabled
            )
        ).fold(
            onSuccess = { answer -> answer.content },
            onError = { error -> throw ComparisonException(errorMessageMapper.map(error)) }
        )

    private suspend fun executeMode(
        task: String,
        mode: ReasoningMode,
        isDeepReasoningEnabled: Boolean,
        updateResult: suspend (
            ReasoningMode,
            (ReasoningResult) -> ReasoningResult
        ) -> Unit
    ): String = when (mode) {
        ReasoningMode.Direct -> runRequest(
            mode = mode,
            prompt = promptFactory.directPrompt(task),
            options = requestOptions(
                maxTokens = DIRECT_MAX_TOKENS,
                isDeepReasoningEnabled = isDeepReasoningEnabled,
                isGuided = false
            ),
            updateResult = updateResult
        )

        ReasoningMode.StepByStep -> runRequest(
            mode = mode,
            prompt = promptFactory.stepByStepPrompt(task),
            options = requestOptions(
                maxTokens = GUIDED_MAX_TOKENS,
                isDeepReasoningEnabled = isDeepReasoningEnabled
            ),
            updateResult = updateResult
        )

        ReasoningMode.PromptGenerated -> runPromptGeneratedMode(
            task = task,
            isDeepReasoningEnabled = isDeepReasoningEnabled,
            updateResult = updateResult
        )

        ReasoningMode.ExpertGroup -> runRequest(
            mode = mode,
            prompt = promptFactory.expertPrompt(task),
            options = requestOptions(
                maxTokens = EXPERT_MAX_TOKENS,
                isDeepReasoningEnabled = isDeepReasoningEnabled
            ),
            updateResult = updateResult
        )
    }

    private suspend fun runRequest(
        mode: ReasoningMode,
        prompt: String,
        options: ChatRequestOptions,
        updateResult: suspend (
            ReasoningMode,
            (ReasoningResult) -> ReasoningResult
        ) -> Unit
    ): String {
        updateResult(mode) { result ->
            result.copy(
                prompt = prompt,
                answer = "",
                reasoningContent = null,
                isLoading = true,
                error = null
            )
        }

        return sendDetailedChatMessageUseCase(
                message = prompt,
                options = options
            ).fold(
            onSuccess = { answer ->
                updateResult(mode) { result ->
                    result.copy(
                        answer = answer.content,
                        reasoningContent = answer.reasoningContent,
                        isLoading = false
                    )
                }
                answer.content
            },
            onError = { appError ->
                val error = errorMessageMapper.map(appError)
                updateResult(mode) { result ->
                    result.copy(
                        isLoading = false,
                        error = error
                    )
                }
                promptFactory.failedAnswer(error)
            }
        )
    }

    private suspend fun runPromptGeneratedMode(
        task: String,
        isDeepReasoningEnabled: Boolean,
        updateResult: suspend (
            ReasoningMode,
            (ReasoningResult) -> ReasoningResult
        ) -> Unit
    ): String {
        val promptGenerationPrompt = promptFactory.promptGenerationPrompt(task)

        updateResult(ReasoningMode.PromptGenerated) { result ->
            result.copy(
                prompt = promptGenerationPrompt,
                answer = "",
                reasoningContent = null,
                isLoading = true,
                error = null
            )
        }

        return sendDetailedChatMessageUseCase(
                message = promptGenerationPrompt,
                options = requestOptions(
                    maxTokens = PROMPT_GENERATION_MAX_TOKENS,
                    isDeepReasoningEnabled = isDeepReasoningEnabled
                )
            ).fold(
            onSuccess = { generatedPromptAnswer ->
                val generatedPrompt = generatedPromptAnswer.content
                updateResult(ReasoningMode.PromptGenerated) { result ->
                    result.copy(prompt = generatedPrompt)
                }

                sendDetailedChatMessageUseCase(
                        message = promptFactory.promptBasedPrompt(generatedPrompt),
                        options = requestOptions(
                            maxTokens = GUIDED_MAX_TOKENS,
                            isDeepReasoningEnabled = isDeepReasoningEnabled
                        )
                    ).fold(
                    onSuccess = { answer ->
                        updateResult(ReasoningMode.PromptGenerated) { result ->
                            result.copy(
                                answer = answer.content,
                                reasoningContent = answer.reasoningContent,
                                isLoading = false
                            )
                        }
                        answer.content
                    },
                    onError = { appError ->
                        val error = errorMessageMapper.map(appError)
                        updateResult(ReasoningMode.PromptGenerated) { result ->
                            result.copy(
                                isLoading = false,
                                error = error
                            )
                        }
                        promptFactory.failedAnswer(error)
                    }
                )
            },
            onError = { appError ->
                val error = errorMessageMapper.map(appError)
                updateResult(ReasoningMode.PromptGenerated) { result ->
                    result.copy(
                        isLoading = false,
                        error = error
                    )
                }
                promptFactory.failedAnswer(error)
            }
        )
    }

    private fun initialResult(
        task: String,
        mode: ReasoningMode
    ): ReasoningResult =
        ReasoningResult(
            mode = mode,
            title = promptFactory.title(mode),
            prompt = mode.initialPrompt(task),
            answer = ""
        )

    private fun ReasoningMode.initialPrompt(task: String): String = when (this) {
        ReasoningMode.Direct -> promptFactory.directPrompt(task)
        ReasoningMode.StepByStep -> promptFactory.stepByStepPrompt(task)
        ReasoningMode.PromptGenerated -> promptFactory.promptGenerationPrompt(task)
        ReasoningMode.ExpertGroup -> promptFactory.expertPrompt(task)
    }

    private fun Set<ReasoningMode>.ordered(): List<ReasoningMode> =
        ReasoningMode.entries.filter { mode -> contains(mode) }

    private fun requestOptions(
        maxTokens: Int,
        isDeepReasoningEnabled: Boolean,
        isGuided: Boolean = true
    ): ChatRequestOptions =
        if (isDeepReasoningEnabled) {
            ChatRequestOptions(
                model = DeepSeekModel.Reasoner,
                maxTokens = maxTokens * REASONER_TOKEN_MULTIPLIER
            )
        } else {
            ChatRequestOptions(
                model = DeepSeekModel.Fast,
                temperature = GUIDED_TEMPERATURE.takeIf { isGuided },
                maxTokens = maxTokens,
                thinkingMode = ThinkingMode.Disabled.takeIf { isGuided }
            )
        }

    private companion object {
        const val GUIDED_TEMPERATURE = 0.2
        const val DIRECT_MAX_TOKENS = 900
        const val GUIDED_MAX_TOKENS = 900
        const val PROMPT_GENERATION_MAX_TOKENS = 500
        const val EXPERT_MAX_TOKENS = 1200
        const val COMPARISON_MAX_TOKENS = 700
        const val REASONER_TOKEN_MULTIPLIER = 3
    }
}

private class ComparisonException(
    override val message: String
) : Exception(message)
