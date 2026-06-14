package com.example.core.domain.usecase

import com.example.core.common.AppResult
import com.example.core.domain.agent.ConversationSummarizer
import com.example.core.domain.agent.HistoryCompressor
import com.example.core.domain.repository.ChatHistoryRepository
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import javax.inject.Inject

/**
 * Готовит контекст запроса с учётом сжатия истории (Day 9): при необходимости сворачивает старую
 * часть диалога в summary (отдельным LLM-вызовом), сохраняет свёртку в Room и возвращает то, что
 * реально уйдёт в модель — `memory (summary)` + последние N сырых сообщений.
 *
 * Если сжатие выключено в конфиге — возвращает историю как есть (поведение Day 7/8).
 */
class PrepareCompressedContextUseCase @Inject constructor(
    private val historyRepository: ChatHistoryRepository,
    private val summarizer: ConversationSummarizer
) {
    data class Result(
        /** Сжатая память (summary), которую агент подставит в system prompt. null — без сжатия. */
        val memory: String?,
        /** Сообщения, реально уходящие в модель сырыми (хвост истории). */
        val recent: List<AgentChatMessage>,
        /** Сколько старых сообщений сейчас представлено summary (для UI «свёрнуто K сообщений»). */
        val summarizedCount: Int
    )

    suspend operator fun invoke(
        conversationId: Long,
        conversation: List<AgentChatMessage>,
        config: AgentConfig
    ): Result {
        if (!config.compressionEnabled) {
            return Result(memory = null, recent = conversation, summarizedCount = 0)
        }

        val state = historyRepository.getSummaryState(conversationId)
        val plan = HistoryCompressor.plan(
            conversation = conversation,
            alreadySummarized = state.summarizedCount,
            keepRecent = config.keepRecentMessages,
            batch = config.summarizeBatch
        )

        if (!plan.needsSummary) {
            return Result(
                memory = state.summary?.takeIf { it.isNotBlank() },
                recent = plan.recent,
                summarizedCount = state.summarizedCount
            )
        }

        return when (val summary = summarizer.summarize(state.summary, plan.toSummarize)) {
            is AppResult.Success -> {
                historyRepository.updateSummary(
                    conversationId = conversationId,
                    summary = summary.data.summary,
                    summarizedCount = plan.summarizedCount
                )
                Result(
                    memory = summary.data.summary.takeIf { it.isNotBlank() },
                    recent = plan.recent,
                    summarizedCount = plan.summarizedCount
                )
            }

            // Свёртка не удалась — не теряем диалог: оставляем прежний summary, а в модель шлём
            // всё, что после прежней границы свёртки (безопасный фолбэк, как «без сжатия отсюда»).
            is AppResult.Error -> Result(
                memory = state.summary?.takeIf { it.isNotBlank() },
                recent = conversation.drop(state.summarizedCount),
                summarizedCount = state.summarizedCount
            )
        }
    }
}
