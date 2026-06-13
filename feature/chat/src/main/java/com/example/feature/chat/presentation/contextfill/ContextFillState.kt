package com.example.feature.chat.presentation.contextfill

/** Что именно демонстрируем баннером. */
enum class ContextFillMode {
    /** Фоновый авто-диалог: постепенная потеря контекста (имя выпадает из окна). */
    AutoDialog,

    /** Один огромный промпт → реальная ошибка переполнения окна от модели. */
    Overflow
}

/** Чем закончилась демо-заливка контекста. */
enum class ContextFillFinish {
    /** Реальная ошибка провайдера (overflow / rate-limit) — то, ради чего и затевалось. */
    Error,

    /** Достигнут защитный лимит ходов (ошибка так и не пришла). */
    MaxTurns,

    /** Остановлено пользователем. */
    Cancelled
}

/**
 * Состояние демо-заливки контекстного окна. `active` управляет видимостью баннера
 * поверх чата; пока `running == true` идёт фоновый авто-диалог.
 */
data class ContextFillUiState(
    val active: Boolean = false,
    val running: Boolean = false,
    /** Что демонстрируем: авто-диалог или одно-разовое переполнение. */
    val mode: ContextFillMode = ContextFillMode.AutoDialog,
    /** Модель, на которой идёт тест (DeepSeek). */
    val modelTitle: String? = null,
    /** Лимит контекстного окна модели (для режима переполнения). */
    val modelWindow: Int = 0,
    /** Имя, которое назвали агенту в начале. */
    val name: String = "",
    /** Номер хода (отправленного визового вопроса). */
    val turn: Int = 0,
    /** Оценка токенов контекста, реально уходящего в модель на последнем ходе. */
    val tokensSent: Int = 0,
    /** Сколько старых сообщений уже выпало из окна (sliding window). */
    val droppedCount: Int = 0,
    /** Сообщение с именем ещё внутри окна? */
    val nameInWindow: Boolean = true,
    /** Помнит ли модель имя по последней проверке (null — ещё не проверяли). */
    val nameRecalled: Boolean? = null,
    /** Транзиентная подсказка статуса (например, пауза из-за rate-limit). */
    val note: String? = null,
    val finish: ContextFillFinish? = null,
    /** Текст реальной ошибки провайдера, если заливка завершилась ошибкой. */
    val errorText: String? = null
) {
    companion object {
        val Idle = ContextFillUiState()
    }
}
