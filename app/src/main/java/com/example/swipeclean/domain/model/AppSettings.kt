package com.example.swipeclean.domain.model

/**
 * Snapshot of user-configurable settings.
 *
 * Phase 1 surfaces the keys actually used by the Swipe / Bin / Haptics
 * subsystems. Phase 2 adds the user-facing Settings screen and exposes
 * the rest (theme, autoplay, notifications, onboarding flag).
 */
data class AppSettings(
    val hapticsEnabled: Boolean = true,
    val hapticIntensity: HapticIntensity = HapticIntensity.MEDIUM,
    val mediaFilter: MediaFilter = MediaFilter.BOTH,
    val binAutoDeleteDays: Int = DEFAULT_BIN_DAYS,
    val minFileSizeBytes: Long = 0L,
    val themeMode: ThemeMode = ThemeMode.SYSTEM,
    val autoplayVideos: Boolean = true,
    val muteVideosByDefault: Boolean = true,
    val binNotificationsEnabled: Boolean = true,
    val showSwipeActionBar: Boolean = true,
    val onboardingComplete: Boolean = false
) {
    companion object {
        const val DEFAULT_BIN_DAYS = 30
    }
}
