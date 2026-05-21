package com.example.swipeclean.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.swipeclean.data.local.db.entities.BinnedMediaEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface BinDao {

    @Query("SELECT * FROM binned_media ORDER BY binnedAt DESC")
    fun observeAll(): Flow<List<BinnedMediaEntity>>

    @Query("SELECT * FROM binned_media ORDER BY binnedAt DESC")
    suspend fun getAll(): List<BinnedMediaEntity>

    @Query("SELECT * FROM binned_media WHERE mediaId = :mediaId")
    suspend fun getById(mediaId: Long): BinnedMediaEntity?

    @Query("SELECT mediaId FROM binned_media")
    fun observeAllIds(): Flow<List<Long>>

    @Query("SELECT mediaId FROM binned_media")
    suspend fun getAllIds(): List<Long>

    @Query("SELECT * FROM binned_media WHERE binnedAt < :threshold")
    suspend fun getExpired(threshold: Long): List<BinnedMediaEntity>

    @Query("SELECT COUNT(*) FROM binned_media")
    fun observeCount(): Flow<Int>

    @Query("SELECT COALESCE(SUM(sizeBytes), 0) FROM binned_media")
    fun observeTotalSize(): Flow<Long>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(entity: BinnedMediaEntity)

    @Query("DELETE FROM binned_media WHERE mediaId = :mediaId")
    suspend fun deleteById(mediaId: Long)

    @Query("DELETE FROM binned_media WHERE mediaId IN (:mediaIds)")
    suspend fun deleteByIds(mediaIds: List<Long>)

    @Query("DELETE FROM binned_media")
    suspend fun clear()
}
