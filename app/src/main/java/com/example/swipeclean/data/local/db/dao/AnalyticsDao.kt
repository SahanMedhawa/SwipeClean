package com.example.swipeclean.data.local.db.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.swipeclean.data.local.db.entities.AnalyticsCounterEntity
import com.example.swipeclean.data.local.db.entities.FolderCountEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface AnalyticsDao {

    @Query("SELECT * FROM analytics_counter WHERE id = 1")
    fun observeCounters(): Flow<AnalyticsCounterEntity?>

    @Query("SELECT * FROM analytics_counter WHERE id = 1")
    suspend fun getCounters(): AnalyticsCounterEntity?

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertCounters(entity: AnalyticsCounterEntity)

    @Update
    suspend fun updateCounters(entity: AnalyticsCounterEntity)

    @Query("DELETE FROM analytics_counter")
    suspend fun clearCounters()

    @Query("SELECT * FROM folder_counts ORDER BY cleanedCount DESC LIMIT :limit")
    fun observeTopFolders(limit: Int): Flow<List<FolderCountEntity>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertFolder(entity: FolderCountEntity)

    @Query("SELECT * FROM folder_counts WHERE folderName = :name")
    suspend fun getFolder(name: String): FolderCountEntity?

    @Query("DELETE FROM folder_counts")
    suspend fun clearFolders()
}
