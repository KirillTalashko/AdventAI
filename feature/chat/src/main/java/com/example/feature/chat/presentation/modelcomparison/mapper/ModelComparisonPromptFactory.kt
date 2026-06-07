package com.example.feature.chat.presentation.modelcomparison.mapper

import com.example.feature.chat.R
import com.example.feature.chat.presentation.modelcomparison.model.ModelComparisonResult
import com.example.feature.chat.presentation.modelcomparison.model.ModelTier
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class ModelComparisonPromptFactory @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun title(tier: ModelTier): String =
        textProvider.get(tier.titleResId)

    fun label(tier: ModelTier): String =
        textProvider.get(tier.labelResId)

    fun docsUrl(tier: ModelTier): String =
        textProvider.get(tier.docsUrlResId)

    fun comparisonPrompt(
        prompt: String,
        results: Map<ModelTier, ModelComparisonResult>
    ): String {
        val selectedAnswers = ModelTier.entries
            .mapNotNull { tier -> results[tier] }
            .filter { result -> result.answer.isNotBlank() }
            .joinToString(separator = "\n\n") { result ->
                textProvider.get(
                    R.string.model_answer_template,
                    result.title,
                    result.elapsedMillis ?: 0L,
                    result.usage?.totalTokens ?: 0,
                    result.estimatedCostUsd ?: 0.0,
                    result.answer
                )
            }

        return textProvider.get(
            R.string.model_comparison_prompt_template,
            prompt,
            selectedAnswers
        )
    }
}
