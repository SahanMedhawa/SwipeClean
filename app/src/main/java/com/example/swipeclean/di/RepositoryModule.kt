package com.example.swipeclean.di

import com.example.swipeclean.data.repository.AnalyticsRepositoryImpl
import com.example.swipeclean.data.repository.BinRepositoryImpl
import com.example.swipeclean.data.repository.KeepFavoriteRepositoryImpl
import com.example.swipeclean.data.repository.MediaRepositoryImpl
import com.example.swipeclean.data.repository.SettingsRepositoryImpl
import com.example.swipeclean.data.repository.SwipeHistoryRepositoryImpl
import com.example.swipeclean.domain.repository.AnalyticsRepository
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.KeepFavoriteRepository
import com.example.swipeclean.domain.repository.MediaRepository
import com.example.swipeclean.domain.repository.SettingsRepository
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import dagger.Binds
import dagger.Module
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class RepositoryModule {

    @Binds @Singleton
    abstract fun bindMediaRepository(impl: MediaRepositoryImpl): MediaRepository

    @Binds @Singleton
    abstract fun bindBinRepository(impl: BinRepositoryImpl): BinRepository

    @Binds @Singleton
    abstract fun bindKeepFavoriteRepository(impl: KeepFavoriteRepositoryImpl): KeepFavoriteRepository

    @Binds @Singleton
    abstract fun bindSwipeHistoryRepository(impl: SwipeHistoryRepositoryImpl): SwipeHistoryRepository

    @Binds @Singleton
    abstract fun bindSettingsRepository(impl: SettingsRepositoryImpl): SettingsRepository

    @Binds @Singleton
    abstract fun bindAnalyticsRepository(impl: AnalyticsRepositoryImpl): AnalyticsRepository
}
