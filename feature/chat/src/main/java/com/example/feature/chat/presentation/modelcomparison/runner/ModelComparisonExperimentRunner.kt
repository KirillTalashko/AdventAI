package com.example.feature.chat.presentation.modelcomparison.runner

import android.os.SystemClock
import com.example.core.common.fold
import com.example.core.domain.usecase.SendChatMessageUseCase
import com.example.core.domain.usecase.SendDetailedChatMessageUseCase
import com.example.core.domain.usecase.SendOpenRouterChatMessageUseCase
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.ThinkingMode
import com.example.core.model.ai.TokenUsage
import com.example.feature.chat.R
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import com.example.feature.chat.presentation.modelcomparison.mapper.ModelComparisonPromptFactory
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonResult
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonUiState
import com.example.feature.chat.presentation.modelcomparison.model.ModelProvider
import com.example.feature.chat.presentation.modelcomparison.model.ModelTier
import com.example.feature.chat.presentation.text.ChatTextProvider
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

class ModelComparisonExperimentRunner @Inject constructor(
    private val sendDetailedChatMessageUseCase: SendDetailedChatMessageUseCase,
    private val sendOpenRouterChatMessageUseCase: SendOpenRouterChatMessageUseCase,
    private val sendChatMessageUseCase: SendChatMessageUseCase,
    private val promptFactory: ModelComparisonPromptFactory,
    private val errorMessageMapper: ChatErrorMessageMapper,
    private val textProvider: ChatTextProvider
) {
    fun run(prompt: String): Flow<ModelComparisonUiState> = flow {
        val sourcePrompt = prompt.trim()
        var state = ModelComparisonUiState(
            prompt = sourcePrompt,
            results = ModelTier.entries.associateWith(::initialResult),
            isLoadingAnswers = true
        )

        emit(state)

        suspend fun updateResult(
            tier: ModelTier,
            transform: (ModelComparisonResult) -> ModelComparisonResult
        ) {
            state = state.copy(
                results = state.results.toMutableMap().apply {
                    put(tier, transform(getValue(tier)))
                }
            )
            emit(state)
        }

        ModelTier.entries.forEach { tier ->
            updateResult(tier) { result ->
                result.copy(
                    answer = "",
                    usage = null,
                    elapsedMillis = null,
                    estimatedCostUsd = null,
                    isLoading = true,
                    error = null
                )
            }

            val startedAt = SystemClock.elapsedRealtime()
            sendTierMessage(
                tier = tier,
                message = sourcePrompt
            ).fold(
                onSuccess = { answer ->
                    val elapsedMillis = SystemClock.elapsedRealtime() - startedAt
                    updateResult(tier) { result ->
                        result.copy(
                            answer = answer.content,
                            usage = answer.usage,
                            elapsedMillis = elapsedMillis,
                            estimatedCostUsd = answer.usage?.estimateCost(tier),
                            isLoading = false
                        )
                    }
                },
                onError = { error ->
                    val elapsedMillis = SystemClock.elapsedRealtime() - startedAt
                    updateResult(tier) { result ->
                        result.copy(
                            elapsedMillis = elapsedMillis,
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
                    error = textProvider.get(R.string.error_no_model_answers)
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
                model = ModelTier.DeepSeekBaseline.deepSeekModel ?: error("DeepSeek model is required"),
                thinkingMode = ThinkingMode.Disabled,
                temperature = COMPARISON_TEMPERATURE,
                maxTokens = COMPARISON_MAX_TOKENS
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

    private suspend fun sendTierMessage(
        tier: ModelTier,
        message: String
    ) = when (tier.provider) {
        ModelProvider.DeepSeek -> sendDetailedChatMessageUseCase(
            message = message,
            options = ChatRequestOptions(
                model = tier.deepSeekModel ?: error("DeepSeek model is required"),
                thinkingMode = tier.thinkingMode,
                maxTokens = ANSWER_MAX_TOKENS
            )
        )

        ModelProvider.OpenRouter -> sendOpenRouterChatMessageUseCase(
            message = message,
            modelId = tier.modelId,
            options = ChatRequestOptions(
                maxTokens = ANSWER_MAX_TOKENS
            )
        )
    }

    private fun initialResult(tier: ModelTier): ModelComparisonResult =
        ModelComparisonResult(
            tier = tier,
            title = promptFactory.title(tier),
            label = promptFactory.label(tier),
            docsUrl = promptFactory.docsUrl(tier)
        )

    private fun TokenUsage.estimateCost(tier: ModelTier): Double {
        val inputCacheHitCost = promptCacheHitTokens * tier.inputCacheHitUsdPerMillion / TOKENS_PER_MILLION
        val inputCacheMissCost = promptCacheMissTokens * tier.inputCacheMissUsdPerMillion / TOKENS_PER_MILLION
        val outputCost = completionTokens * tier.outputUsdPerMillion / TOKENS_PER_MILLION

        return inputCacheHitCost + inputCacheMissCost + outputCost
    }

    private companion object {
        const val ANSWER_MAX_TOKENS = 900
        const val COMPARISON_MAX_TOKENS = 900
        const val COMPARISON_TEMPERATURE = 0.0
        const val TOKENS_PER_MILLION = 1_000_000.0
    }
}
