package com.example.swipeclean.data.local.db

import androidx.room.Database
import androidx.room.RoomDatabase
import com.example.swipeclean.data.local.db.dao.AnalyticsDao
import com.example.swipeclean.data.local.db.dao.BinDao
import com.example.swipeclean.data.local.db.dao.KeepFavoriteDao
import com.example.swipeclean.data.local.db.dao.SwipeActionDao
import com.example.swipeclean.data.local.db.entities.AnalyticsCounterEntity
import com.example.swipeclean.data.local.db.entities.BinnedMediaEntity
import com.example.swipeclean.data.local.db.entities.FolderCountEntity
import com.example.swipeclean.data.local.db.entities.KeepFavoriteEntity
import com.example.swipeclean.data.local.db.entities.SwipeActionEntity

@Database(
    entities = [
        BinnedMediaEntity::class,
        SwipeActionEntity::class,
        KeepFavoriteEntity::class,
        AnalyticsCounterEntity::class,
        FolderCountEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class SwipeCleanDatabase : RoomDatabase() {
    abstract fun binDao(): BinDao
    abstract fun swipeActionDao(): SwipeActionDao
    abstract fun keepFavoriteDao(): KeepFavoriteDao
    abstract fun analyticsDao(): AnalyticsDao

    companion object {
        const val NAME = "swipeclean.db"
    }
}
