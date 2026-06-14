package com.example.feature.chat.presentation.compression.runner

import com.example.core.common.AppResult
import com.example.core.domain.agent.AiAgent
import com.example.core.domain.agent.ConversationSummarizer
import com.example.core.domain.agent.HistoryCompressor
import com.example.core.domain.agent.TokenEstimator
import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentConfig
import com.example.core.model.ai.AgentLlmModel
import com.example.core.model.ai.AgentMessageAuthor
import com.example.core.model.ai.ThinkingMode
import com.example.core.model.ai.TokenUsage
import com.example.feature.chat.presentation.compression.model.AbRunResult
import com.example.feature.chat.presentation.compression.model.AbSummarizationEvent
import com.example.feature.chat.presentation.compression.model.AbVariant
import com.example.feature.chat.presentation.compression.model.CompressionAbUiState
import com.example.feature.chat.presentation.contextfill.ContextFillQuestions
import com.example.feature.chat.presentation.mapper.ChatErrorMessageMapper
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import javax.inject.Inject

/**
 * Прогоняет один и тот же сценарий диалога дважды и собирает A/B-сравнение сжатия истории (Day 9):
 *
 *  - **Baseline** — в модель уходит вся история целиком (без сжатия);
 *  - **Compressed** — старая часть свёрнута в summary ([ConversationSummarizer]), шлём
 *    `summary + последние N` ([HistoryCompressor]).
 *
 * Сценарий: называем имя → серия «тяжёлых» визовых вопросов (забивают контекст) → финальная
 * проверка «как меня зовут?». На выходе видно качество (вспомнил ли имя), расход токенов, размер
 * контекста по ходам, стоимость с кэшем и без, а также сам процесс свёртки (таймлайн).
 *
 * Эксперимент изолированный: история живёт в памяти прогона, в Room ничего не пишется.
 */
class CompressionAbRunner @Inject constructor(
    private val agent: AiAgent,
    private val summarizer: ConversationSummarizer,
    private val errorMessageMapper: ChatErrorMessageMapper
) {
    fun run(): Flow<CompressionAbUiState> = flow {
        val config = baseConfig()
        val questions = ContextFillQuestions.bank
        var state = CompressionAbUiState(isRunning = true, turnsTotal = TURNS)
        emit(state)

        for (variant in listOf(AbVariant.Baseline, AbVariant.Compressed)) {
            state = state.copy(activeVariant = variant)
            emit(state)

            val conversation = mutableListOf<AgentChatMessage>()
            var summary: String? = null
            var summarizedCount = 0
            var run = AbRunResult(variant)

            // Один ход «ответа агента». turnLabel — для таймлайна свёрток. false при ошибке провайдера.
            suspend fun ask(turnLabel: Int): Boolean {
                val recent: List<AgentChatMessage>
                val memory: String?
                if (variant == AbVariant.Compressed) {
                    val plan = HistoryCompressor.plan(
                        conversation = conversation,
                        alreadySummarized = summarizedCount,
                        keepRecent = config.keepRecentMessages,
                        batch = config.summarizeBatch
                    )
                    if (plan.needsSummary) {
                        when (val s = summarizer.summarize(summary, plan.toSummarize)) {
                            is AppResult.Success -> {
                                summary = s.data.summary
                                summarizedCount = plan.summarizedCount
                                s.data.usage?.let { run = run.addOverhead(it) }
                                // «До»: сырой размер всех свёрнутых сообщений; «после»: размер summary.
                                val rawBefore = conversation.take(plan.summarizedCount)
                                    .sumOf { TokenEstimator.estimateMessage(it.text) }
                                val batchTokens = plan.toSummarize
                                    .sumOf { TokenEstimator.estimateMessage(it.text) }
                                run = run.copy(
                                    summarizations = run.summarizations + AbSummarizationEvent(
                                        afterTurn = turnLabel,
                                        foldedCount = plan.toSummarize.size,
                                        totalSummarized = plan.summarizedCount,
                                        foldedPreview = preview(plan.toSummarize),
                                        summary = s.data.summary,
                                        rawTokensBefore = rawBefore,
                                        batchTokens = batchTokens,
                                        summaryTokens = TokenEstimator.estimateText(s.data.summary)
                                    )
                                )
                            }

                            is AppResult.Error -> {
                                state = state.copy(error = errorMessageMapper.map(s.error), isRunning = false)
                                emit(state)
                                return false
                            }
                        }
                    }
                    recent = plan.recent
                    memory = summary
                } else {
                    recent = conversation.toList()
                    memory = null
                }

                return when (val answer = agent.ask(config, recent, memory)) {
                    is AppResult.Success -> {
                        conversation += AgentChatMessage(
                            author = AgentMessageAuthor.Agent,
                            text = answer.data.content,
                            createdAt = System.currentTimeMillis(),
                            usage = answer.data.usage,
                            modelApiId = config.model.apiId
                        )
                        answer.data.usage?.let { usage ->
                            run = run.addAnswer(usage)
                            run = run.copy(contextSeries = run.contextSeries + usage.promptTokens)
                        }
                        true
                    }

                    is AppResult.Error -> {
                        state = state.copy(error = errorMessageMapper.map(answer.error), isRunning = false)
                        emit(state)
                        false
                    }
                }
            }

            // 1. Представляемся — имя должно «дожить» до конца через историю или summary.
            conversation += userMessage(
                "Меня зовут $TEST_NAME. Запомни моё имя — в конце диалога я спрошу, как меня зовут."
            )
            if (!ask(turnLabel = 0)) return@flow
            state = state.withRun(run)
            emit(state)
            delay(PACE_MS)

            // 2. Серия «тяжёлых» вопросов — забивают контекст, провоцируют свёртку в сжатом прогоне.
            for (index in 0 until TURNS) {
                conversation += userMessage(
                    ContextFillQuestions.weighty(questions[index % questions.size], index)
                )
                if (!ask(turnLabel = index + 1)) return@flow
                run = run.copy(turnsDone = index + 1)
                state = state.withRun(run)
                emit(state)
                delay(PACE_MS)
            }

            // 3. Проверка памяти: вспомнит ли агент имя.
            conversation += userMessage("Кстати, как меня зовут? Ответь только именем.")
            if (!ask(turnLabel = TURNS + 1)) return@flow
            val nameAnswer = conversation.last().text
            run = run.copy(
                nameAnswer = nameAnswer.trim().take(NAME_ANSWER_MAX_CHARS),
                nameRecalled = nameAnswer.contains(TEST_NAME, ignoreCase = true),
                finished = true
            )
            state = state.withRun(run)
            emit(state)
        }

        state = state.copy(isRunning = false, finished = true, activeVariant = null)
        emit(state)
    }

    private fun CompressionAbUiState.withRun(run: AbRunResult): CompressionAbUiState =
        when (run.variant) {
            AbVariant.Baseline -> copy(baseline = run)
            AbVariant.Compressed -> copy(compressed = run)
        }

    private fun AbRunResult.addAnswer(usage: TokenUsage): AbRunResult = copy(
        promptTokens = promptTokens + usage.promptTokens,
        completionTokens = completionTokens + usage.completionTokens,
        cacheHitTokens = cacheHitTokens + usage.promptCacheHitTokens,
        costUsd = costUsd + cost(usage, withCache = true),
        costNoCacheUsd = costNoCacheUsd + cost(usage, withCache = false)
    )

    private fun AbRunResult.addOverhead(usage: TokenUsage): AbRunResult = copy(
        overheadTokens = overheadTokens + usage.totalTokens,
        cacheHitTokens = cacheHitTokens + usage.promptCacheHitTokens,
        costUsd = costUsd + cost(usage, withCache = true),
        costNoCacheUsd = costNoCacheUsd + cost(usage, withCache = false)
    )

    private fun cost(usage: TokenUsage, withCache: Boolean): Double =
        PRICING.costUsd(
            promptTokens = usage.promptTokens,
            cacheHitTokens = if (withCache) usage.promptCacheHitTokens else 0,
            completionTokens = usage.completionTokens
        )

    /** Короткое превью свёрнутой пачки: первое сообщение (часто — имя) + сколько ещё. */
    private fun preview(messages: List<AgentChatMessage>): String {
        val first = messages.firstOrNull()?.let { msg ->
            val who = if (msg.author == AgentMessageAuthor.User) "Вы" else "Агент"
            "$who: ${msg.text.trim().replace('\n', ' ').take(PREVIEW_MAX_CHARS)}"
        } ?: ""
        val rest = (messages.size - 1).coerceAtLeast(0)
        return if (rest > 0) "$first … (+$rest)" else first
    }

    private fun userMessage(text: String): AgentChatMessage =
        AgentChatMessage(
            author = AgentMessageAuthor.User,
            text = text,
            createdAt = System.currentTimeMillis()
        )

    private fun baseConfig(): AgentConfig = AgentConfig(
        name = "Визовый специалист",
        model = AgentLlmModel.DeepSeekFlash,
        dialogTheme = "Консультация по визам, документам и подготовке к подаче",
        systemPrompt = "Ты визовый специалист. Помогай разобраться с требованиями, документами, " +
            "сроками и рисками. Отвечай практично и по делу.",
        keepRecentMessages = KEEP_RECENT,
        summarizeBatch = BATCH,
        maxTokens = ANSWER_MAX_TOKENS,
        // Reasoning отключаем: иначе при малом maxTokens он съедает бюджет и ответ не успевает
        // сформироваться (TokenLimitReached). Для A/B нам важен расход на истории/summary, не «думанье».
        thinkingMode = ThinkingMode.Disabled
    )

    private companion object {
        const val TEST_NAME = "Кирилл"
        const val TURNS = 12
        const val KEEP_RECENT = 4
        const val BATCH = 6
        const val ANSWER_MAX_TOKENS = 700
        const val PACE_MS = 300L
        const val PREVIEW_MAX_CHARS = 60
        const val NAME_ANSWER_MAX_CHARS = 120
        val PRICING = AgentLlmModel.DeepSeekFlash.pricing
    }
}
