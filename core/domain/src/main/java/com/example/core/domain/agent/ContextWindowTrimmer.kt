package com.example.core.domain.agent

import com.example.core.model.ai.AgentChatMessage

/**
 * Sliding-window обрезка истории под бюджет токенов контекстного окна.
 *
 * Используется демо-заливкой контекста (Day 9): когда история перерастает окно, в модель уходит
 * только «хвост» (последние сообщения), а самые старые (включая представление с именем в начале)
 * выпадают — на этом и строится демонстрация «модель забыла, как меня зовут».
 *
 * Считаем через тот же [TokenEstimator], что и индикатор окна, чтобы оценка совпадала с UI.
 * Последнее сообщение сохраняется всегда (это текущий запрос пользователя).
 */
object ContextWindowTrimmer {

    data class Result(
        /** Сообщения, реально уходящие в модель (укладываются в бюджет). */
        val kept: List<AgentChatMessage>,
        /** Сколько старых сообщений выпало из окна (для разделителя «вне окна» в UI). */
        val droppedCount: Int,
        /** Оценка токенов отправляемого контекста (system prompt + kept). */
        val estimatedTokens: Int
    )

    fun trim(
        systemPrompt: String?,
        conversation: List<AgentChatMessage>,
        budgetTokens: Int
    ): Result {
        val base = TokenEstimator.estimateConversation(systemPrompt, emptyList())
        if (conversation.isEmpty()) {
            return Result(kept = emptyList(), droppedCount = 0, estimatedTokens = base)
        }

        val perMessage = conversation.map { TokenEstimator.estimateMessage(it.text) }
        var running = perMessage.sum()
        var start = 0
        // Выкидываем самые старые, пока контекст не уложится в бюджет; последнее сообщение не трогаем.
        while (start < conversation.lastIndex && base + running > budgetTokens) {
            running -= perMessage[start]
            start++
        }

        return Result(
            kept = conversation.subList(start, conversation.size).toList(),
            droppedCount = start,
            estimatedTokens = base + running
        )
    }
}
