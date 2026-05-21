package com.example.swipeclean.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.swipeclean.data.local.db.entities.KeepFavoriteEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface KeepFavoriteDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(entity: KeepFavoriteEntity)

    @Query("SELECT mediaId FROM keep_favorite")
    fun observeAllIds(): Flow<List<Long>>

    @Query("SELECT mediaId FROM keep_favorite")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM keep_favorite WHERE mediaId = :mediaId")
    suspend fun getById(mediaId: Long): KeepFavoriteEntity?

    @Query("DELETE FROM keep_favorite WHERE mediaId = :mediaId")
    suspend fun deleteById(mediaId: Long)

    @Query("DELETE FROM keep_favorite")
    suspend fun clear()
}
