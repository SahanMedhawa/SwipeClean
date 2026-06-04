package com.example.swipeclean.ui.onboarding

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.repository.SettingsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class OnboardingViewModel @Inject constructor(
    private val settingsRepository: SettingsRepository
) : ViewModel() {

    val state: StateFlow<OnboardingUiState> = settingsRepository.settings
        .map { OnboardingUiState(loading = false, complete = it.onboardingComplete) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            OnboardingUiState(loading = true)
        )

    fun complete() {
        viewModelScope.launch {
            settingsRepository.setOnboardingComplete(true)
        }
    }
}

data class OnboardingUiState(
    val loading: Boolean = true,
    val complete: Boolean = false
)
