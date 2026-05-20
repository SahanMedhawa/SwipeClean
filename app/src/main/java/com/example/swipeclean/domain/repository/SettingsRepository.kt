package com.example.swipeclean.domain.repository

import com.example.swipeclean.domain.model.AppSettings
import com.example.swipeclean.domain.model.HapticIntensity
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.ThemeMode
import kotlinx.coroutines.flow.Flow

interface SettingsRepository {
    val settings: Flow<AppSettings>
    suspend fun current(): AppSettings
    suspend fun setHapticsEnabled(value: Boolean)
    suspend fun setHapticIntensity(value: HapticIntensity)
    suspend fun setMediaFilter(value: MediaFilter)
    suspend fun setBinAutoDeleteDays(days: Int)
    suspend fun setMinFileSizeBytes(bytes: Long)
    suspend fun setThemeMode(value: ThemeMode)
    suspend fun setAutoplayVideos(value: Boolean)
    suspend fun setBinNotificationsEnabled(value: Boolean)
    suspend fun setOnboardingComplete(value: Boolean)
}
