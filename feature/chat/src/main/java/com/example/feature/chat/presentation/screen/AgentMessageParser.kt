package com.example.feature.chat.presentation.screen

import com.example.core.designsystem.component.ChecklistItemUi
import com.example.core.designsystem.component.DocumentStatus

/**
 * Часть ответа агента: либо обычный текст, либо распознанный чек-лист документов.
 * «Богатые сообщения» (SCREEN_REQUIREMENTS.md §4.2) — вместо сырого markdown.
 */
sealed interface AgentMessagePart {
    data class Text(val text: String) : AgentMessagePart
    data class Checklist(val items: List<ChecklistItemUi>) : AgentMessagePart
}

private val CHECKLIST_BLOCK = Regex(
    """\[checklist]\s*(.*?)\s*\[/checklist]""",
    setOf(RegexOption.IGNORE_CASE, RegexOption.DOT_MATCHES_ALL)
)

/**
 * Толерантный парсер: ищет блоки `[checklist]…[/checklist]` по договорённости из system prompt
 * (см. ChatViewModel). Если блока нет или он пуст — возвращает обычный текст без потерь.
 */
fun parseAgentMessage(raw: String): List<AgentMessagePart> {
    val matches = CHECKLIST_BLOCK.findAll(raw).toList()
    if (matches.isEmpty()) {
        return listOf(AgentMessagePart.Text(raw.trim()))
    }

    val parts = mutableListOf<AgentMessagePart>()
    var cursor = 0
    for (match in matches) {
        val before = raw.substring(cursor, match.range.first).trim()
        if (before.isNotEmpty()) {
            parts += AgentMessagePart.Text(before)
        }
        val items = parseChecklistItems(match.groupValues[1])
        if (items.isNotEmpty()) {
            parts += AgentMessagePart.Checklist(items)
        }
        cursor = match.range.last + 1
    }
    val tail = raw.substring(cursor).trim()
    if (tail.isNotEmpty()) {
        parts += AgentMessagePart.Text(tail)
    }
    return parts.ifEmpty { listOf(AgentMessagePart.Text(raw.trim())) }
}

private fun parseChecklistItems(body: String): List<ChecklistItemUi> =
    body.lineSequence()
        .map { it.trim().removePrefix("-").removePrefix("•").trim() }
        .filter { it.isNotEmpty() }
        .mapNotNull { line ->
            val columns = line.split(";", "|", "—").map { it.trim() }
            val title = columns.firstOrNull()?.takeIf { it.isNotEmpty() } ?: return@mapNotNull null
            val status = columns.getOrNull(1).toDocumentStatus()
            ChecklistItemUi(title = title, status = status)
        }
        .toList()

private fun String?.toDocumentStatus(): DocumentStatus = when {
    this == null -> DocumentStatus.Needed
    contains("провер", ignoreCase = true) -> DocumentStatus.Verified
    contains("загруж", ignoreCase = true) -> DocumentStatus.Uploaded
    else -> DocumentStatus.Needed
}
