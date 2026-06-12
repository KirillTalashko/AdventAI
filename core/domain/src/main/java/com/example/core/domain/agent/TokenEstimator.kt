package com.example.core.domain.agent

import kotlin.math.ceil

/**
 * Локальная оценка количества токенов **до** отправки запроса. Нужна, чтобы:
 *  - показывать заполнение контекстного окна по мере роста диалога;
 *  - предупреждать о переполнении окна заранее (API вернёт цифры только постфактум).
 *
 * Это эвристика, а не настоящий BPE-токенайзер модели: кириллица «дороже» латиницы,
 * поэтому считаем их с разным коэффициентом. Факт берём из `usage` ответа API и
 * сравниваем с оценкой — расхождение и есть учебная «соль» Day 8.
 */
object TokenEstimator {
    /** Кириллица обычно ~2 символа на токен. */
    private const val CYRILLIC_CHARS_PER_TOKEN = 2.0

    /** Латиница/цифры/пробелы/пунктуация — ~4 символа на токен. */
    private const val OTHER_CHARS_PER_TOKEN = 4.0

    /** Накладные токены на обёртку одного сообщения (роль/разделители). */
    private const val MESSAGE_OVERHEAD_TOKENS = 4

    /** Служебные токены «подводки» ответа ассистента. */
    private const val REPLY_PRIMING_TOKENS = 3

    fun estimateText(text: String): Int {
        if (text.isEmpty()) return 0
        var tokens = 0.0
        for (ch in text) {
            tokens += if (ch.isCyrillic()) {
                1.0 / CYRILLIC_CHARS_PER_TOKEN
            } else {
                1.0 / OTHER_CHARS_PER_TOKEN
            }
        }
        return ceil(tokens).toInt()
    }

    fun estimateMessage(text: String): Int = estimateText(text) + MESSAGE_OVERHEAD_TOKENS

    /**
     * Оценка всего запроса: system prompt + история сообщений + подводка ответа.
     * Это и есть оценка `prompt_tokens`, которые улетят в модель.
     */
    fun estimateConversation(systemPrompt: String?, messageTexts: List<String>): Int {
        var total = REPLY_PRIMING_TOKENS
        if (!systemPrompt.isNullOrBlank()) {
            total += estimateMessage(systemPrompt)
        }
        messageTexts.forEach { total += estimateMessage(it) }
        return total
    }

    private fun Char.isCyrillic(): Boolean = this in 'Ѐ'..'ӿ'
}
