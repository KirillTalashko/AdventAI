package com.example.core.data.ai.local

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Вопрос-наполнитель для демо-заливки контекстного окна. Таблица — session-scoped:
 * заполняется при запуске заливки и очищается в конце сессии (когда умирает Activity).
 */
@Entity(tableName = "filler_questions")
data class FillerQuestionEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    @ColumnInfo(name = "text")
    val text: String,
    @ColumnInfo(name = "used")
    val used: Boolean = false
)
