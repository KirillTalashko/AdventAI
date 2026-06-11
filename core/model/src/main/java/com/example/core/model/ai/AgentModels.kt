package com.example.core.model.ai

data class AgentConfig(
    val name: String,
    val model: AgentLlmModel,
    val systemPrompt: String,
    val dialogTheme: String
)

enum class AgentProvider {
    DeepSeek,
    OpenRouter
}

enum class AgentLlmModel(
    val title: String,
    val apiId: String,
    val provider: AgentProvider,
    val deepSeekModel: DeepSeekModel? = null
) {
    DeepSeekFlash(
        title = "DeepSeek V4 Flash",
        apiId = DeepSeekModel.Fast.apiName,
        provider = AgentProvider.DeepSeek,
        deepSeekModel = DeepSeekModel.Fast
    ),
    DeepSeekPro(
        title = "DeepSeek V4 Pro",
        apiId = DeepSeekModel.Pro.apiName,
        provider = AgentProvider.DeepSeek,
        deepSeekModel = DeepSeekModel.Pro
    ),
    LlamaFree(
        title = "Llama 3.3 70B",
        apiId = "meta-llama/llama-3.3-70b-instruct:free",
        provider = AgentProvider.OpenRouter
    ),
    QwenFree(
        title = "Qwen3 Next 80B",
        apiId = "qwen/qwen3-next-80b-a3b-instruct:free",
        provider = AgentProvider.OpenRouter
    )
}

data class AgentChatMessage(
    val author: AgentMessageAuthor,
    val text: String,
    /** Момент создания (epoch ms). 0 = ещё не присвоен (проставится при сохранении). */
    val createdAt: Long = 0L
)

enum class AgentMessageAuthor {
    User,
    Agent
}

data class AgentAnswer(
    val content: String,
    val modelTitle: String
)
