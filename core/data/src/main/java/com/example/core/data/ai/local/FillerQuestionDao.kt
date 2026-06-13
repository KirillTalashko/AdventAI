package com.example.core.data.ai.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query

@Dao
interface FillerQuestionDao {
    @Query("SELECT COUNT(*) FROM filler_questions")
    suspend fun count(): Int

    @Insert
    suspend fun insertAll(questions: List<FillerQuestionEntity>)

    @Query("SELECT * FROM filler_questions WHERE used = 0 ORDER BY id ASC")
    suspend fun getUnused(): List<FillerQuestionEntity>

    @Query("UPDATE filler_questions SET used = 1 WHERE id = :id")
    suspend fun markUsed(id: Long)

    @Query("DELETE FROM filler_questions")
    suspend fun clear()
}
