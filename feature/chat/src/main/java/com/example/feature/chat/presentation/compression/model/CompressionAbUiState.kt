package com.example.feature.chat.presentation.compression.model

/** Какой из двух прогонов A/B-сравнения. */
enum class AbVariant {
    /** Без сжатия — в модель уходит вся история целиком. */
    Baseline,

    /** Со сжатием — старая часть свёрнута в summary, шлём `summary + последние N`. */
    Compressed
}

/**
 * Событие свёртки (только у сжатого прогона): что и когда свернулось в summary. Нужно, чтобы
 * наглядно показать сам процесс сжатия и что summary хранится отдельно от сообщений.
 */
data class AbSummarizationEvent(
    /** После какого хода произошла свёртка. */
    val afterTurn: Int,
    /** Сколько сообщений свернулось в этот раз. */
    val foldedCount: Int,
    /** Сколько всего сообщений теперь представлено summary. */
    val totalSummarized: Int,
    /** Короткое превью свёрнутых сообщений. */
    val foldedPreview: String,
    /** Получившийся summary (целиком — хранится отдельно от истории). */
    val summary: String,
    /** «До»: сколько токенов занимали все свёрнутые сообщения (сырой размер истории). */
    val rawTokensBefore: Int,
    /** Токены пачки, свёрнутой именно в этот раз. */
    val batchTokens: Int,
    /** «После»: оценка размера summary в токенах. */
    val summaryTokens: Int
) {
    /** На сколько процентов сжалась история этим summary (до → после). */
    val savedPercent: Int
        get() = if (rawTokensBefore > 0 && summaryTokens < rawTokensBefore) {
            ((rawTokensBefore - summaryTokens) * 100) / rawTokensBefore
        } else {
            0
        }
}

/**
 * Итог одного прогона сценария. Токены — факт из `usage` API, просуммированный по ходам.
 *
 * Стоимость считаем в двух вариантах: [costUsd] — реальная цена DeepSeek (с учётом кэша промпта,
 * который сильно удешевляет переотправку одной и той же истории) и [costNoCacheUsd] — как если бы
 * кэша не было (полная цена входа). Это и объясняет, почему «дешевле по сырым токенам» ≠ «дешевле
 * по деньгам» на коротком диалоге с кэшем.
 */
data class AbRunResult(
    val variant: AbVariant,
    val turnsDone: Int = 0,
    /** Суммарный вход (prompt) по ответам агента. */
    val promptTokens: Int = 0,
    /** Суммарный выход (completion) по ответам агента. */
    val completionTokens: Int = 0,
    /** Токены, ушедшие на запросы суммаризации (накладные сжатия). */
    val overheadTokens: Int = 0,
    /** Сколько входных токенов пришло из кэша промпта. */
    val cacheHitTokens: Int = 0,
    /** Стоимость всех вызовов с учётом кэша (реальная цена DeepSeek), USD. */
    val costUsd: Double = 0.0,
    /** Стоимость, как если бы кэша не было (полная цена входа), USD. */
    val costNoCacheUsd: Double = 0.0,
    /** Размер контекста, ушедшего в модель на каждом ходе (prompt tokens) — для графика роста. */
    val contextSeries: List<Int> = emptyList(),
    /** События свёртки (только у сжатого прогона). */
    val summarizations: List<AbSummarizationEvent> = emptyList(),
    /** Вспомнил ли агент имя в финальной проверке (null — ещё не дошли). */
    val nameRecalled: Boolean? = null,
    /** Текст финального ответа на «как меня зовут?» (наглядное доказательство памяти). */
    val nameAnswer: String? = null,
    val finished: Boolean = false
) {
    /** Все токены прогона: вход + выход ответов + накладные на свёртки. */
    val totalTokens: Int get() = promptTokens + completionTokens + overheadTokens

    /** Пиковый размер контекста (последний ход) — насколько «раздут» запрос. */
    val peakContextTokens: Int get() = contextSeries.maxOrNull() ?: 0
}

/**
 * Состояние A/B-эксперимента сжатия истории (Day 9). Один и тот же сценарий прогоняется дважды —
 * без сжатия и со сжатием — а результаты показываются рядом (токены, стоимость, память, процесс).
 */
data class CompressionAbUiState(
    val isRunning: Boolean = false,
    val turnsTotal: Int = 0,
    /** Какой прогон сейчас идёт (для подсветки), null — простаиваем/завершено. */
    val activeVariant: AbVariant? = null,
    val baseline: AbRunResult = AbRunResult(AbVariant.Baseline),
    val compressed: AbRunResult = AbRunResult(AbVariant.Compressed),
    val finished: Boolean = false,
    val error: String? = null
) {
    /** Экономия по входным токенам, % (compressed vs baseline). */
    val promptTokensSavedPercent: Int get() = percentSaved(baseline.promptTokens, compressed.promptTokens)

    /** Экономия по всем токенам (с учётом накладных на свёртки), %. */
    val totalTokensSavedPercent: Int get() = percentSaved(baseline.totalTokens, compressed.totalTokens)

    /** Экономия по размеру контекста на последнем ходе, %. */
    val contextSavedPercent: Int get() = percentSaved(baseline.peakContextTokens, compressed.peakContextTokens)

    /** Экономия по стоимости С КЭШЕМ, % (может быть 0/отрицательной — кэш удешевляет полную историю). */
    val costSavedPercent: Int get() = percentSaved(baseline.costUsd, compressed.costUsd)

    /** Экономия по стоимости БЕЗ КЭША, % (здесь сжатие выигрывает заметнее). */
    val costNoCacheSavedPercent: Int get() = percentSaved(baseline.costNoCacheUsd, compressed.costNoCacheUsd)

    /** Стоимость со сжатием получилась выше из-за кэша промпта? */
    val compressionCostlierWithCache: Boolean
        get() = hasConclusion && compressed.costUsd > baseline.costUsd

    /** Есть ли уже готовый итог для финального блока вывода. */
    val hasConclusion: Boolean get() = baseline.finished && compressed.finished

    private fun percentSaved(base: Int, now: Int): Int =
        if (base > 0 && now < base) ((base - now) * 100) / base else 0

    private fun percentSaved(base: Double, now: Double): Int =
        if (base > 0.0 && now < base) (((base - now) / base) * 100).toInt() else 0
}
