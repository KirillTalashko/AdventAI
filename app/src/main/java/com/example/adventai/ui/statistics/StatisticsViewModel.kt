package com.example.adventai.ui.statistics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.core.domain.repository.ChatHistoryRepository
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.ConversationTokenStat
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import javax.inject.Inject

@OptIn(ExperimentalCoroutinesApi::class)
@HiltViewModel
class StatisticsViewModel @Inject constructor(
    private val chatHistoryRepository: ChatHistoryRepository
) : ViewModel() {

    val uiState =
        chatHistoryRepository.observeTokenStats()
            .flatMapLatest { stats ->
                seriesFlow(stats).map { series -> buildUiState(stats, series) }
            }
            .stateIn(
                scope = viewModelScope,
                started = SharingStarted.WhileSubscribed(5_000),
                initialValue = StatisticsUiState()
            )

    /** Поток ряда «рост токенов» для самого длинного диалога. */
    private fun seriesFlow(stats: List<ConversationTokenStat>): Flow<List<TokenSeriesPoint>> {
        val top = stats.filter { it.messageCount > 0 }.maxByOrNull { it.messageCount }
            ?: return flowOf(emptyList())
        return chatHistoryRepository.observeMessages(top.conversationId).map { messages ->
            buildSeries(messages)
        }
    }

    private fun buildSeries(messages: List<AgentChatMessage>): List<TokenSeriesPoint> {
        var cumulative = 0
        var turn = 0
        return buildList {
            messages.forEach { message ->
                val usage = message.usage ?: return@forEach
                turn++
                cumulative += usage.totalTokens
                add(
                    TokenSeriesPoint(
                        turn = turn,
                        totalTokens = usage.totalTokens,
                        cumulativeTokens = cumulative
                    )
                )
            }
        }
    }

    private fun buildUiState(
        stats: List<ConversationTokenStat>,
        series: List<TokenSeriesPoint>
    ): StatisticsUiState {
        val withUsage = stats.filter { it.messageCount > 0 }
        val dialogs = withUsage.map { it.toUi() }
        val topTitle = withUsage.maxByOrNull { it.messageCount }?.title
        return StatisticsUiState(
            hasData = withUsage.isNotEmpty(),
            totalTokens = withUsage.sumOf { it.totalTokens },
            totalPromptTokens = withUsage.sumOf { it.promptTokens },
            totalCompletionTokens = withUsage.sumOf { it.completionTokens },
            totalCacheHitTokens = withUsage.sumOf { it.cacheHitTokens },
            totalRequests = withUsage.sumOf { it.messageCount },
            dialogCount = withUsage.size,
            totalCostUsd = dialogs.sumOf { it.costUsd },
            dialogs = dialogs,
            series = series,
            seriesTitle = topTitle
        )
    }

    private fun ConversationTokenStat.toUi(): DialogUsageUi {
        val model = AgentLlmModel.fromApiId(modelApiId)
        val cost = model?.pricing?.costUsd(
            promptTokens = promptTokens,
            cacheHitTokens = cacheHitTokens,
            completionTokens = completionTokens
        ) ?: 0.0
        return DialogUsageUi(
            conversationId = conversationId,
            title = title,
            modelTitle = model?.title,
            requests = messageCount,
            promptTokens = promptTokens,
            completionTokens = completionTokens,
            totalTokens = totalTokens,
            cacheHitTokens = cacheHitTokens,
            costUsd = cost
        )
    }
}

data class StatisticsUiState(
    val hasData: Boolean = false,
    val totalTokens: Int = 0,
    val totalPromptTokens: Int = 0,
    val totalCompletionTokens: Int = 0,
    val totalCacheHitTokens: Int = 0,
    val totalRequests: Int = 0,
    val dialogCount: Int = 0,
    val totalCostUsd: Double = 0.0,
    val dialogs: List<DialogUsageUi> = emptyList(),
    val series: List<TokenSeriesPoint> = emptyList(),
    val seriesTitle: String? = null
)

data class DialogUsageUi(
    val conversationId: Long,
    val title: String,
    val modelTitle: String?,
    val requests: Int,
    val promptTokens: Int,
    val completionTokens: Int,
    val totalTokens: Int,
    val cacheHitTokens: Int,
    val costUsd: Double
)

data class TokenSeriesPoint(
    val turn: Int,
    val totalTokens: Int,
    val cumulativeTokens: Int
)
