package com.example.swipeclean.di

import android.content.Context
import androidx.room.Room
import com.example.swipeclean.data.local.db.SwipeCleanDatabase
import com.example.swipeclean.data.local.db.dao.AnalyticsDao
import com.example.swipeclean.data.local.db.dao.BinDao
import com.example.swipeclean.data.local.db.dao.KeepFavoriteDao
import com.example.swipeclean.data.local.db.dao.SwipeActionDao
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.android.qualifiers.ApplicationContext
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object DatabaseModule {

    @Provides
    @Singleton
    fun provideDatabase(@ApplicationContext context: Context): SwipeCleanDatabase =
        Room.databaseBuilder(context, SwipeCleanDatabase::class.java, SwipeCleanDatabase.NAME)
            // Phase 1: schema is fresh, no migrations yet. When we change the schema
            // we'll replace this with proper Migration objects.
            .fallbackToDestructiveMigration()
            .build()

    @Provides fun provideBinDao(db: SwipeCleanDatabase): BinDao = db.binDao()

    @Provides fun provideSwipeActionDao(db: SwipeCleanDatabase): SwipeActionDao = db.swipeActionDao()

    @Provides fun provideKeepFavoriteDao(db: SwipeCleanDatabase): KeepFavoriteDao = db.keepFavoriteDao()

    @Provides fun provideAnalyticsDao(db: SwipeCleanDatabase): AnalyticsDao = db.analyticsDao()
}
