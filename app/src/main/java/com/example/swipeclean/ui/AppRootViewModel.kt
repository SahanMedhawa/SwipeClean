package com.example.swipeclean.ui

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.model.ThemeMode
import com.example.swipeclean.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

/**
 * Reads global app state needed before the nav graph mounts: the
 * onboarding-complete flag (to pick the start destination) and the theme
 * mode (to drive [com.example.swipeclean.ui.theme.SwipeCleanTheme]).
 */
@HiltViewModel
class AppRootViewModel @Inject constructor(
    settingsRepository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<AppRootState> = settingsRepository.settings
        .map { settings ->
            AppRootState(
                loading = false,
                onboardingComplete = settings.onboardingComplete,
                themeMode = settings.themeMode
            )
        }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AppRootState(loading = true)
        )
}

data class AppRootState(
    val loading: Boolean = true,
    val onboardingComplete: Boolean = false,
    val themeMode: ThemeMode = ThemeMode.SYSTEM
)
