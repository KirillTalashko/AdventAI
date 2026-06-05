package com.example.feature.chat.presentation.reasoning.mapper

import com.example.feature.chat.R
import com.example.feature.chat.presentation.reasoning.model.ReasoningMode
import com.example.feature.chat.presentation.reasoning.model.ReasoningResult
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class ReasoningPromptFactory @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun title(mode: ReasoningMode): String =
        textProvider.get(mode.titleResId)

    fun directPrompt(task: String): String =
        textProvider.get(
            R.string.reasoning_direct_prompt_template,
            task.trim()
        )

    fun stepByStepPrompt(task: String): String =
        textProvider.get(
            R.string.reasoning_step_by_step_prompt_template,
            task.trim()
        )

    fun promptGenerationPrompt(task: String): String =
        textProvider.get(
            R.string.reasoning_prompt_generation_template,
            task.trim()
        )

    fun promptBasedPrompt(generatedPrompt: String): String = generatedPrompt.trim()

    fun expertPrompt(task: String): String =
        textProvider.get(
            R.string.reasoning_expert_prompt_template,
            task.trim()
        )

    fun comparisonPrompt(
        task: String,
        results: Map<ReasoningMode, ReasoningResult>
    ): String =
        textProvider.get(
            R.string.reasoning_comparison_template,
            task.trim(),
            results.toSelectedAnswersText()
        )

    fun failedAnswer(error: String): String =
        textProvider.get(R.string.reasoning_failed_answer_template, error)

    private fun Map<ReasoningMode, ReasoningResult>.toSelectedAnswersText(): String =
        ReasoningMode.entries
            .mapNotNull { mode ->
                val answer = get(mode)?.answer?.takeIf(String::isNotBlank)
                    ?: return@mapNotNull null

                textProvider.get(
                    R.string.reasoning_selected_answer_template,
                    title(mode),
                    answer
                )
            }
            .joinToString(separator = "\n\n")
}
