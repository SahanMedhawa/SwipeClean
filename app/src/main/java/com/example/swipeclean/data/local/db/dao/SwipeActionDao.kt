package com.example.swipeclean.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.Query
import com.example.swipeclean.data.local.db.entities.SwipeActionEntity

@Dao
interface SwipeActionDao {

    @Insert
    suspend fun insert(entity: SwipeActionEntity): Long

    @Query("SELECT * FROM swipe_actions WHERE sessionId = :sessionId ORDER BY id DESC LIMIT :limit")
    suspend fun recentForSession(sessionId: Long, limit: Int = 10): List<SwipeActionEntity>

    @Query("SELECT * FROM swipe_actions WHERE sessionId = :sessionId ORDER BY id DESC LIMIT 1")
    suspend fun lastForSession(sessionId: Long): SwipeActionEntity?

    @Query("DELETE FROM swipe_actions WHERE id = :id")
    suspend fun deleteById(id: Long)

    @Query("DELETE FROM swipe_actions WHERE sessionId != :sessionId")
    suspend fun clearAllExceptSession(sessionId: Long)

    @Query("DELETE FROM swipe_actions")
    suspend fun clear()
}
