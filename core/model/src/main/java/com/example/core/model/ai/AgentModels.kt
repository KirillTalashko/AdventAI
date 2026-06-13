package com.example.core.model.ai

data class AgentConfig(
    val name: String,
    val model: AgentLlmModel,
    val systemPrompt: String,
    val dialogTheme: String,
    /**
     * Демонстрационный лимит контекстного окна (в токенах). `null` — использовать
     * реальный лимит модели. Маленькое значение позволяет показать переполнение
     * окна на коротком диалоге (Day 8).
     */
    val demoContextLimitTokens: Int? = null,
    /** Параметры API. `null` — значение по умолчанию у провайдера. */
    val temperature: Double? = null,
    val maxTokens: Int? = null,
    val topP: Double? = null
)

/** Эффективный лимит окна: демо-значение, но не больше реального лимита модели. */
fun AgentConfig.effectiveContextLimit(): Int {
    val modelLimit = model.maxContextTokens
    val demo = demoContextLimitTokens ?: return modelLimit
    return demo.coerceIn(1, modelLimit)
}

enum class AgentProvider {
    DeepSeek,
    OpenRouter
}

/**
 * Тариф модели (USD за 1M токенов). У DeepSeek вход с попаданием в кэш промпта
 * дешевле, чем без него, поэтому считаем их отдельно. Бесплатные модели → [Free].
 *
 * Значения — ориентир для демонстрации стоимости; перед продакшеном сверять с
 * официальным прайсингом провайдера.
 */
data class ModelPricing(
    val inputPerMillion: Double,
    val cachedInputPerMillion: Double,
    val outputPerMillion: Double
) {
    val isFree: Boolean get() = inputPerMillion == 0.0 && outputPerMillion == 0.0

    /** Стоимость одного запроса по факту использования токенов. */
    fun costUsd(promptTokens: Int, cacheHitTokens: Int, completionTokens: Int): Double {
        val missTokens = (promptTokens - cacheHitTokens).coerceAtLeast(0)
        return missTokens / 1_000_000.0 * inputPerMillion +
            cacheHitTokens / 1_000_000.0 * cachedInputPerMillion +
            completionTokens / 1_000_000.0 * outputPerMillion
    }

    companion object {
        val Free = ModelPricing(0.0, 0.0, 0.0)
    }
}

enum class AgentLlmModel(
    val title: String,
    val apiId: String,
    val provider: AgentProvider,
    val maxContextTokens: Int,
    val pricing: ModelPricing,
    val deepSeekModel: DeepSeekModel? = null
) {
    DeepSeekFlash(
        title = "DeepSeek V4 Flash",
        apiId = DeepSeekModel.Fast.apiName,
        provider = AgentProvider.DeepSeek,
        maxContextTokens = 65_536,
        pricing = ModelPricing(
            inputPerMillion = 0.27,
            cachedInputPerMillion = 0.07,
            outputPerMillion = 1.10
        ),
        deepSeekModel = DeepSeekModel.Fast
    ),
    DeepSeekPro(
        title = "DeepSeek V4 Pro",
        apiId = DeepSeekModel.Pro.apiName,
        provider = AgentProvider.DeepSeek,
        maxContextTokens = 131_072,
        pricing = ModelPricing(
            inputPerMillion = 0.55,
            cachedInputPerMillion = 0.14,
            outputPerMillion = 2.19
        ),
        deepSeekModel = DeepSeekModel.Pro
    ),
    LlamaFree(
        title = "Llama 3.3 70B",
        apiId = "meta-llama/llama-3.3-70b-instruct:free",
        provider = AgentProvider.OpenRouter,
        maxContextTokens = 131_072,
        pricing = ModelPricing.Free
    ),
    QwenFree(
        title = "Qwen3 Next 80B",
        apiId = "qwen/qwen3-next-80b-a3b-instruct:free",
        provider = AgentProvider.OpenRouter,
        maxContextTokens = 131_072,
        pricing = ModelPricing.Free
    ),
    GemmaFree(
        title = "Gemma 4 31B",
        apiId = "google/gemma-4-31b-it:free",
        provider = AgentProvider.OpenRouter,
        maxContextTokens = 131_072,
        pricing = ModelPricing.Free
    ),
    GptOssFree(
        title = "GPT-OSS 20B",
        apiId = "openai/gpt-oss-20b:free",
        provider = AgentProvider.OpenRouter,
        maxContextTokens = 131_072,
        pricing = ModelPricing.Free
    );

    companion object {
        /** Найти модель по её API-идентификатору (для подсчёта стоимости в статистике). */
        fun fromApiId(apiId: String?): AgentLlmModel? =
            apiId?.let { id -> entries.firstOrNull { it.apiId == id } }

        /**
         * Бесплатная модель OpenRouter для демо-заливки контекста: заливать окно на платной
         * DeepSeek дорого, поэтому стресс-тест всегда идёт на free-провайдере.
         */
        fun freeOpenRouter(): AgentLlmModel =
            entries.firstOrNull { it.provider == AgentProvider.OpenRouter && it.pricing.isFree }
                ?: LlamaFree
    }
}

data class AgentChatMessage(
    val author: AgentMessageAuthor,
    val text: String,
    /** Момент создания (epoch ms). 0 = ещё не присвоен (проставится при сохранении). */
    val createdAt: Long = 0L,
    /** Расход токенов на этот ответ модели (только у сообщений агента). */
    val usage: TokenUsage? = null,
    /** Какой моделью получен ответ — нужно для подсчёта стоимости. */
    val modelApiId: String? = null
)

enum class AgentMessageAuthor {
    User,
    Agent
}

data class AgentAnswer(
    val content: String,
    val modelTitle: String,
    /** Токены запроса/ответа от API — пробрасываем в UI, не выбрасываем. */
    val usage: TokenUsage? = null
)
