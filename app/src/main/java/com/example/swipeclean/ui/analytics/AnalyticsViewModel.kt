package com.example.swipeclean.ui.analytics

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.model.AnalyticsSummary
import com.example.swipeclean.domain.repository.AnalyticsRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn

@HiltViewModel
class AnalyticsViewModel @Inject constructor(
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    val state: StateFlow<AnalyticsUiState> = analyticsRepository.observeSummary()
        .map { AnalyticsUiState(summary = it, loading = false) }
        .stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            AnalyticsUiState(loading = true)
        )
}

data class AnalyticsUiState(
    val loading: Boolean = true,
    val summary: AnalyticsSummary = AnalyticsSummary()
)
