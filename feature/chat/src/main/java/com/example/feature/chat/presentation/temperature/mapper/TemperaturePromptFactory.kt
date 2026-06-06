package com.example.feature.chat.presentation.temperature.mapper

import com.example.feature.chat.R
import com.example.feature.chat.presentation.temperature.model.TemperatureMode
import com.example.feature.chat.presentation.temperature.model.TemperatureResult
import com.example.feature.chat.presentation.text.ChatTextProvider
import javax.inject.Inject

class TemperaturePromptFactory @Inject constructor(
    private val textProvider: ChatTextProvider
) {
    fun title(mode: TemperatureMode): String =
        textProvider.get(mode.titleResId)

    fun label(mode: TemperatureMode): String =
        textProvider.get(mode.labelResId)

    fun comparisonPrompt(
        prompt: String,
        results: Map<TemperatureMode, TemperatureResult>
    ): String {
        val selectedAnswers = TemperatureMode.entries
            .mapNotNull { mode -> results[mode] }
            .filter { result -> result.answer.isNotBlank() }
            .joinToString(separator = "\n\n") { result ->
                textProvider.get(
                    R.string.temperature_answer_template,
                    result.title,
                    result.answer
                )
            }

        return textProvider.get(
            R.string.temperature_comparison_prompt_template,
            prompt,
            selectedAnswers
        )
    }
}
