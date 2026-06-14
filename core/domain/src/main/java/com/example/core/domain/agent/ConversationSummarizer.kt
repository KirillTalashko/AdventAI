package com.example.core.domain.agent

import com.example.core.common.AppResult
import com.example.core.domain.usecase.SendDetailedChatMessageUseCase
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.ChatRequestOptions
import com.example.core.model.ai.DeepSeekModel
import com.example.core.model.ai.ThinkingMode
import com.example.core.model.ai.TokenUsage
import javax.inject.Inject

/**
 * Сворачивает старую часть истории в краткое содержание (summary) отдельным запросом к LLM.
 *
 * Свёртка **инкрементальная (rolling)**: на вход — предыдущий summary + новая пачка старых
 * сообщений, на выход — обновлённый единый summary. Это держит и стоимость, и размер свёртки
 * ограниченными (не пересуммируем всё заново на каждом ходу).
 *
 * Суммаризатор намеренно работает на дешёвой быстрой модели (DeepSeek V4 Flash) с выключенным
 * reasoning и небольшим лимитом ответа — это служебный вызов, экономия здесь важнее «красоты».
 */
class ConversationSummarizer @Inject constructor(
    private val sendDetailedChatMessage: SendDetailedChatMessageUseCase
) {
    /** Результат свёртки: обновлённый текст summary + токены, потраченные на сам вызов. */
    data class Result(
        val summary: String,
        val usage: TokenUsage?
    )

    /**
     * @param previousSummary текущее краткое содержание (null/пусто — свёртки ещё не было).
     * @param newMessages старые сообщения, которые нужно влить в summary.
     */
    suspend fun summarize(
        previousSummary: String?,
        newMessages: List<AgentChatMessage>
    ): AppResult<Result> {
        if (newMessages.isEmpty()) {
            return AppResult.Success(Result(summary = previousSummary.orEmpty().trim(), usage = null))
        }

        val input = buildSummarizationInput(previousSummary, newMessages)
        return when (
            val answer = sendDetailedChatMessage(
                message = input,
                options = ChatRequestOptions(
                    model = DeepSeekModel.Fast,
                    systemPrompt = SUMMARIZER_SYSTEM_PROMPT,
                    thinkingMode = ThinkingMode.Disabled,
                    temperature = SUMMARY_TEMPERATURE,
                    maxTokens = SUMMARY_MAX_TOKENS
                )
            )
        ) {
            is AppResult.Success -> AppResult.Success(
                Result(summary = answer.data.content.trim(), usage = answer.data.usage)
            )

            is AppResult.Error -> answer
        }
    }

    private fun buildSummarizationInput(
        previousSummary: String?,
        newMessages: List<AgentChatMessage>
    ): String = buildString {
        if (!previousSummary.isNullOrBlank()) {
            append("Предыдущее краткое содержание:\n")
            append(previousSummary.trim())
            append("\n\n")
        }
        append("Новые сообщения, которые нужно добавить в конспект:\n")
        newMessages.forEach { message ->
            append(message.author.label())
            append(": ")
            append(message.text.trim())
            append('\n')
        }
    }

    private fun AgentMessageAuthor.label(): String = when (this) {
        AgentMessageAuthor.User -> "Пользователь"
        AgentMessageAuthor.Agent -> "Агент"
    }

    private companion object {
        const val SUMMARY_TEMPERATURE = 0.0
        const val SUMMARY_MAX_TOKENS = 400

        val SUMMARIZER_SYSTEM_PROMPT = """
            Ты — модуль сжатия истории диалога. На вход тебе дают предыдущее краткое содержание
            (если есть) и новые сообщения. Верни ОБНОВЛЁННОЕ краткое содержание на русском СТРОГО
            в формате двух разделов:

            ФАКТЫ: <через «; » перечисли все конкретные факты — имя пользователя, числа, даты, страны,
            документы, принятые решения, предпочтения. Сначала СКОПИРУЙ дословно все факты из
            предыдущего раздела ФАКТЫ, затем добавь новые из новых сообщений. Факты терять нельзя —
            это долговременная память агента.>
            КОНТЕКСТ: <сжатый пересказ обсуждения, не более ~80 слов, без приветствий и без воды.>

            Не выдумывай факты, которых не было в сообщениях. Не отвечай на вопросы пользователя и не
            давай советов — только конспектируй диалог.
        """.trimIndent()
    }
}
