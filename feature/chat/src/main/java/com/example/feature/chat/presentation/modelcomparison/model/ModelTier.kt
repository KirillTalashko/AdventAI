package com.example.feature.chat.presentation.modelcomparison.model

import androidx.annotation.StringRes
import com.example.core.model.ai.DeepSeekModel
import com.example.core.model.ai.ThinkingMode
import com.example.feature.chat.R

enum class ModelTier(
    val provider: ModelProvider,
    val modelId: String,
    val deepSeekModel: DeepSeekModel?,
    val thinkingMode: ThinkingMode?,
    val inputCacheHitUsdPerMillion: Double,
    val inputCacheMissUsdPerMillion: Double,
    val outputUsdPerMillion: Double,
    @StringRes val titleResId: Int,
    @StringRes val labelResId: Int,
    @StringRes val docsUrlResId: Int
) {
    DeepSeekBaseline(
        provider = ModelProvider.DeepSeek,
        modelId = DeepSeekModel.Fast.apiName,
        deepSeekModel = DeepSeekModel.Fast,
        thinkingMode = ThinkingMode.Disabled,
        inputCacheHitUsdPerMillion = FLASH_INPUT_CACHE_HIT_USD,
        inputCacheMissUsdPerMillion = FLASH_INPUT_CACHE_MISS_USD,
        outputUsdPerMillion = FLASH_OUTPUT_USD,
        titleResId = R.string.model_deepseek_title,
        labelResId = R.string.model_deepseek_label,
        docsUrlResId = R.string.deepseek_pricing_url
    ),
    Llama(
        provider = ModelProvider.OpenRouter,
        modelId = "meta-llama/llama-3.3-70b-instruct:free",
        deepSeekModel = null,
        thinkingMode = null,
        inputCacheHitUsdPerMillion = FREE_USD,
        inputCacheMissUsdPerMillion = FREE_USD,
        outputUsdPerMillion = FREE_USD,
        titleResId = R.string.model_llama_title,
        labelResId = R.string.model_llama_label,
        docsUrlResId = R.string.openrouter_llama_url
    ),
    Gemma(
        provider = ModelProvider.OpenRouter,
        modelId = "google/gemma-4-31b-it:free",
        deepSeekModel = null,
        thinkingMode = null,
        inputCacheHitUsdPerMillion = FREE_USD,
        inputCacheMissUsdPerMillion = FREE_USD,
        outputUsdPerMillion = FREE_USD,
        titleResId = R.string.model_gemma_title,
        labelResId = R.string.model_gemma_label,
        docsUrlResId = R.string.openrouter_gemma_url
    ),
    Qwen(
        provider = ModelProvider.OpenRouter,
        modelId = "qwen/qwen3-next-80b-a3b-instruct:free",
        deepSeekModel = null,
        thinkingMode = null,
        inputCacheHitUsdPerMillion = FREE_USD,
        inputCacheMissUsdPerMillion = FREE_USD,
        outputUsdPerMillion = FREE_USD,
        titleResId = R.string.model_qwen_title,
        labelResId = R.string.model_qwen_label,
        docsUrlResId = R.string.openrouter_qwen_url
    ),
    GptOss(
        provider = ModelProvider.OpenRouter,
        modelId = "openai/gpt-oss-20b:free",
        deepSeekModel = null,
        thinkingMode = null,
        inputCacheHitUsdPerMillion = FREE_USD,
        inputCacheMissUsdPerMillion = FREE_USD,
        outputUsdPerMillion = FREE_USD,
        titleResId = R.string.model_gpt_oss_title,
        labelResId = R.string.model_gpt_oss_label,
        docsUrlResId = R.string.openrouter_gpt_oss_url
    )
}

enum class ModelProvider {
    DeepSeek,
    OpenRouter
}

private const val FLASH_INPUT_CACHE_HIT_USD = 0.0028
private const val FLASH_INPUT_CACHE_MISS_USD = 0.14
private const val FLASH_OUTPUT_USD = 0.28
private const val FREE_USD = 0.0
