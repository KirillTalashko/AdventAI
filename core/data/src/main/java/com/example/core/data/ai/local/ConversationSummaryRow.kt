package com.example.core.data.ai.local

import androidx.room.ColumnInfo

/** Проекция состояния сжатия истории диалога (Day 9). */
data class ConversationSummaryRow(
    @ColumnInfo(name = "summary")
    val summary: String?,
    @ColumnInfo(name = "summarized_count")
    val summarizedCount: Int
)
