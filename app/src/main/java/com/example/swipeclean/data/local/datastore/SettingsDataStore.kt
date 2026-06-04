package com.example.swipeclean.data.local.datastore

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.swipeclean.domain.model.AppSettings
import com.example.swipeclean.domain.model.HapticIntensity
import com.example.swipeclean.domain.model.MediaFilter
import com.example.swipeclean.domain.model.ThemeMode
import dagger.hilt.android.qualifiers.ApplicationContext
import javax.inject.Inject
import javax.inject.Singleton
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

val Context.swipeCleanDataStore: DataStore<Preferences> by preferencesDataStore(name = "swipeclean_settings")

/**
 * Direct DataStore wrapper. The repository layer translates the raw preferences
 * into the [AppSettings] domain model. We keep this class concrete and final
 * because it owns the typed keys.
 */
@Singleton
class SettingsDataStore @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val store get() = context.swipeCleanDataStore

    val settings: Flow<AppSettings> = store.data.map { prefs ->
        AppSettings(
            hapticsEnabled = prefs[Keys.HAPTICS_ENABLED] ?: true,
            hapticIntensity = prefs[Keys.HAPTIC_INTENSITY]?.let {
                runCatching { HapticIntensity.valueOf(it) }.getOrNull()
            } ?: HapticIntensity.MEDIUM,
            mediaFilter = prefs[Keys.MEDIA_FILTER]?.let {
                runCatching { MediaFilter.valueOf(it) }.getOrNull()
            } ?: MediaFilter.BOTH,
            binAutoDeleteDays = prefs[Keys.BIN_DAYS] ?: AppSettings.DEFAULT_BIN_DAYS,
            minFileSizeBytes = prefs[Keys.MIN_FILE_SIZE_BYTES] ?: 0L,
            themeMode = prefs[Keys.THEME_MODE]?.let {
                runCatching { ThemeMode.valueOf(it) }.getOrNull()
            } ?: ThemeMode.SYSTEM,
            autoplayVideos = prefs[Keys.AUTOPLAY_VIDEOS] ?: true,
            muteVideosByDefault = prefs[Keys.MUTE_VIDEOS_BY_DEFAULT] ?: true,
            binNotificationsEnabled = prefs[Keys.BIN_NOTIFICATIONS] ?: true,
            showSwipeActionBar = prefs[Keys.SHOW_SWIPE_ACTION_BAR] ?: true,
            onboardingComplete = prefs[Keys.ONBOARDING_COMPLETE] ?: false
        )
    }

    suspend fun setHapticsEnabled(value: Boolean) {
        store.edit { it[Keys.HAPTICS_ENABLED] = value }
    }

    suspend fun setHapticIntensity(value: HapticIntensity) {
        store.edit { it[Keys.HAPTIC_INTENSITY] = value.name }
    }

    suspend fun setMediaFilter(value: MediaFilter) {
        store.edit { it[Keys.MEDIA_FILTER] = value.name }
    }

    suspend fun setBinAutoDeleteDays(days: Int) {
        store.edit { it[Keys.BIN_DAYS] = days }
    }

    suspend fun setMinFileSizeBytes(bytes: Long) {
        store.edit { it[Keys.MIN_FILE_SIZE_BYTES] = bytes }
    }

    suspend fun setThemeMode(value: ThemeMode) {
        store.edit { it[Keys.THEME_MODE] = value.name }
    }

    suspend fun setAutoplayVideos(value: Boolean) {
        store.edit { it[Keys.AUTOPLAY_VIDEOS] = value }
    }

    suspend fun setMuteVideosByDefault(value: Boolean) {
        store.edit { it[Keys.MUTE_VIDEOS_BY_DEFAULT] = value }
    }

    suspend fun setBinNotificationsEnabled(value: Boolean) {
        store.edit { it[Keys.BIN_NOTIFICATIONS] = value }
    }

    suspend fun setShowSwipeActionBar(value: Boolean) {
        store.edit { it[Keys.SHOW_SWIPE_ACTION_BAR] = value }
    }

    suspend fun setOnboardingComplete(value: Boolean) {
        store.edit { it[Keys.ONBOARDING_COMPLETE] = value }
    }

    private object Keys {
        val HAPTICS_ENABLED = booleanPreferencesKey("haptics_enabled")
        val HAPTIC_INTENSITY = stringPreferencesKey("haptic_intensity")
        val MEDIA_FILTER = stringPreferencesKey("media_filter")
        val BIN_DAYS = intPreferencesKey("bin_auto_delete_days")
        val MIN_FILE_SIZE_BYTES = longPreferencesKey("min_file_size_bytes")
        val THEME_MODE = stringPreferencesKey("theme_mode")
        val AUTOPLAY_VIDEOS = booleanPreferencesKey("autoplay_videos")
        val MUTE_VIDEOS_BY_DEFAULT = booleanPreferencesKey("mute_videos_by_default")
        val BIN_NOTIFICATIONS = booleanPreferencesKey("bin_notifications_enabled")
        val SHOW_SWIPE_ACTION_BAR = booleanPreferencesKey("show_swipe_action_bar")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
    }
}
