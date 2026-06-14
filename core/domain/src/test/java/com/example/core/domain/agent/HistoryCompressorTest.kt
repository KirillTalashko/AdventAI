package com.example.core.domain.agent

import com.example.core.model.ai.AgentChatMessage
import com.example.core.model.ai.AgentMessageAuthor
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistoryCompressorTest {

    private fun history(size: Int): List<AgentChatMessage> =
        (1..size).map { index ->
            AgentChatMessage(
                author = if (index % 2 == 1) AgentMessageAuthor.User else AgentMessageAuthor.Agent,
                text = "msg-$index"
            )
        }

    @Test
    fun `short history below threshold is not summarized`() {
        val plan = HistoryCompressor.plan(
            conversation = history(8),
            alreadySummarized = 0,
            keepRecent = 6,
            batch = 10
        )

        assertFalse(plan.needsSummary)
        assertTrue(plan.toSummarize.isEmpty())
        assertEquals(0, plan.summarizedCount)
        // Без свёртки весь диалог уходит сырым.
        assertEquals(8, plan.recent.size)
    }

    @Test
    fun `summarizes oldest batch when overflow reaches batch size`() {
        // total 16, keepRecent 6 → надо свернуть 10 старых, batch 10 → запускаем свёртку.
        val conversation = history(16)
        val plan = HistoryCompressor.plan(
            conversation = conversation,
            alreadySummarized = 0,
            keepRecent = 6,
            batch = 10
        )

        assertTrue(plan.needsSummary)
        assertEquals(10, plan.toSummarize.size)
        assertEquals("msg-1", plan.toSummarize.first().text)
        assertEquals("msg-10", plan.toSummarize.last().text)
        assertEquals(6, plan.recent.size)
        assertEquals("msg-11", plan.recent.first().text)
        assertEquals(10, plan.summarizedCount)
    }

    @Test
    fun `does not re-summarize until next full batch accumulates`() {
        // Уже свёрнуто 10, total 18 → сверх keepRecent осталось 2 новых (< batch) → свёртки нет.
        val plan = HistoryCompressor.plan(
            conversation = history(18),
            alreadySummarized = 10,
            keepRecent = 6,
            batch = 10
        )

        assertFalse(plan.needsSummary)
        // recent = всё после уже свёрнутого префикса (тут временно длиннее keepRecent).
        assertEquals(8, plan.recent.size)
        assertEquals("msg-11", plan.recent.first().text)
        assertEquals(10, plan.summarizedCount)
    }

    @Test
    fun `recent always keeps the last message`() {
        val plan = HistoryCompressor.plan(
            conversation = history(26),
            alreadySummarized = 10,
            keepRecent = 6,
            batch = 10
        )

        // 26 - 6 = 20 свернуть, уже 10 → ещё 10 (== batch) → свёртка.
        assertTrue(plan.needsSummary)
        assertEquals(20, plan.summarizedCount)
        assertEquals("msg-26", plan.recent.last().text)
        assertEquals(6, plan.recent.size)
    }
}
