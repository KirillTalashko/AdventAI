package com.example.core.domain.agent

import com.example.core.model.ai.AgentChatMessage

/**
 * Чистая логика управления контекстом через сжатие истории (Day 9).
 *
 * Идея: последние N сообщений держим «как есть», всё, что старше, сворачивается в одно
 * краткое содержание (summary) и отправляется в модель вместо полной истории. Чтобы не дёргать
 * LLM на каждое сообщение, свёртку запускаем пачками («каждые [batch] сообщений»).
 *
 * Состояние свёртки на диалог — всего одно число [Plan.summarizedCount]: сколько самых старых
 * сообщений уже представлено текущим summary. Сообщения append-only, порядок стабильный, поэтому
 * счётчика достаточно (id в доменную модель тащить не нужно).
 *
 * Класс ничего не знает про LLM и сеть — только режет список. Свёртку делает [ConversationSummarizer].
 */
object HistoryCompressor {

    data class Plan(
        /** Старые сообщения, которые нужно свернуть в summary прямо сейчас (пусто — свёртка не нужна). */
        val toSummarize: List<AgentChatMessage>,
        /** Сырой «хвост», уходящий в модель как есть (всегда содержит последнее сообщение). */
        val recent: List<AgentChatMessage>,
        /** Новое значение «сколько свёрнуто» после применения плана (для сохранения). */
        val summarizedCount: Int,
        /** Нужно ли запускать свёртку: накопилась пачка старых сообщений. */
        val needsSummary: Boolean
    )

    /**
     * @param conversation полная история диалога по порядку (старые → новые).
     * @param alreadySummarized сколько самых старых сообщений уже покрыто существующим summary.
     * @param keepRecent сколько последних сообщений всегда оставляем сырыми.
     * @param batch минимальный размер новой пачки, при котором запускаем свёртку.
     */
    fun plan(
        conversation: List<AgentChatMessage>,
        alreadySummarized: Int,
        keepRecent: Int,
        batch: Int
    ): Plan {
        val total = conversation.size
        val keep = keepRecent.coerceAtLeast(1)
        val current = alreadySummarized.coerceIn(0, total)
        // Цель: свёрнуто всё, что старше последних keep сообщений.
        val targetSummarized = (total - keep).coerceAtLeast(0)
        val pending = targetSummarized - current

        // Сворачиваем только когда накопилась целая пачка — иначе лишние сырые сообщения
        // пока остаются в хвосте (recent временно длиннее keep, потом схлопнется).
        if (pending >= batch.coerceAtLeast(1)) {
            return Plan(
                toSummarize = conversation.subList(current, targetSummarized).toList(),
                recent = conversation.subList(targetSummarized, total).toList(),
                summarizedCount = targetSummarized,
                needsSummary = true
            )
        }

        // Свёртка не нужна: summary покрывает первые `current`, всё после него идёт сырым.
        return Plan(
            toSummarize = emptyList(),
            recent = conversation.subList(current, total).toList(),
            summarizedCount = current,
            needsSummary = false
        )
    }
}
