package com.example.swipeclean.data.repository

import com.example.swipeclean.data.local.datastore.SettingsDataStore
import com.example.swipeclean.domain.model.AppSettings
import com.example.swipeclean.domain.model.HapticIntensity
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.ThemeMode
import com.example.swipeclean.domain.repository.SettingsRepository
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first

@Singleton
class SettingsRepositoryImpl @Inject constructor(
    private val store: SettingsDataStore
) : SettingsRepository {

    override val settings: Flow<AppSettings> = store.settings

    override suspend fun current(): AppSettings = store.settings.first()

    override suspend fun setHapticsEnabled(value: Boolean) = store.setHapticsEnabled(value)
    override suspend fun setHapticIntensity(value: HapticIntensity) = store.setHapticIntensity(value)
    override suspend fun setMediaFilter(value: MediaFilter) = store.setMediaFilter(value)
    override suspend fun setBinAutoDeleteDays(days: Int) = store.setBinAutoDeleteDays(days)
    override suspend fun setMinFileSizeBytes(bytes: Long) = store.setMinFileSizeBytes(bytes)
    override suspend fun setThemeMode(value: ThemeMode) = store.setThemeMode(value)
    override suspend fun setAutoplayVideos(value: Boolean) = store.setAutoplayVideos(value)
    override suspend fun setBinNotificationsEnabled(value: Boolean) = store.setBinNotificationsEnabled(value)
    override suspend fun setOnboardingComplete(value: Boolean) = store.setOnboardingComplete(value)
}
