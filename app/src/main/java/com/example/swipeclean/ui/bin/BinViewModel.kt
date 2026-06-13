package com.example.swipeclean.ui.bin

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.model.AppSettings
import com.example.swipeclean.domain.model.BinnedMedia
import com.example.swipeclean.domain.repository.AnalyticsRepository
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.SettingsRepository
import com.example.swipeclean.domain.usecase.EmptyBin
import com.example.swipeclean.domain.usecase.PermanentlyDeleteMedia
import com.example.swipeclean.domain.usecase.RestoreFromBin
import com.example.swipeclean.haptics.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

@HiltViewModel
class BinViewModel @Inject constructor(
    private val binRepository: BinRepository,
    private val settingsRepository: SettingsRepository,
    private val emptyBin: EmptyBin,
    private val permanentlyDeleteMedia: PermanentlyDeleteMedia,
    private val restoreFromBin: RestoreFromBin,
    private val intentSenderDispatcher: IntentSenderDispatcher,
    private val hapticManager: HapticManager,
    private val analyticsRepository: AnalyticsRepository
) : ViewModel() {

    private val pendingByTag = mutableMapOf<String, Long>()

    private val settingsFlow = settingsRepository.settings
    private val _showConfirm = MutableStateFlow(false)

    val uiState: StateFlow<BinUiState> = combine(
        binRepository.observeBin(),
        settingsFlow,
        _showConfirm
    ) { items, settings, showConfirm ->
        BinUiState(
            items = items,
            autoDeleteDays = settings.binAutoDeleteDays,
            totalSizeBytes = items.sumOf { it.sizeBytes },
            showConfirm = showConfirm,
            now = System.currentTimeMillis()
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        BinUiState()
    )

    init {
        settingsFlow
            .onEach { settings: AppSettings -> hapticManager.updateSettings(settings) }
            .launchIn(viewModelScope)

        intentSenderDispatcher.results
            .onEach { result ->
                if (result.confirmed && result.mediaIds.isNotEmpty()) {
                    binRepository.confirmPermanentlyDeleted(result.mediaIds)
                    val reclaimed = pendingByTag.remove(result.tag) ?: 0L
                    if (reclaimed > 0) {
                        analyticsRepository.recordPermanentDeletion(reclaimed)
                    }
                    if (result.tag == TAG_EMPTY) {
                        hapticManager.binEmptied()
                    }
                } else {
                    pendingByTag.remove(result.tag)
                }
            }
            .launchIn(viewModelScope)
    }

    fun openConfirmEmpty() {
        _showConfirm.value = true
    }

    fun dismissConfirm() {
        _showConfirm.value = false
    }

    fun requestEmpty() {
        viewModelScope.launch {
            val request = emptyBin.buildIntent() ?: run {
                _showConfirm.value = false
                return@launch
            }
            pendingByTag[TAG_EMPTY] = request.totalBytes
            intentSenderDispatcher.requestDelete(
                BinDeletionRequest(
                    tag = TAG_EMPTY,
                    intentSender = request.intentSender,
                    mediaIds = request.mediaIds,
                    totalBytes = request.totalBytes
                )
            )
            _showConfirm.value = false
        }
    }

    fun restore(mediaId: Long) {
        viewModelScope.launch {
            restoreFromBin(mediaId)
        }
    }

    fun deleteOne(mediaId: Long) {
        viewModelScope.launch {
            val sender = permanentlyDeleteMedia(listOf(mediaId)) ?: return@launch
            val size = binRepository.getAll().firstOrNull { it.mediaId == mediaId }?.sizeBytes ?: 0L
            val tag = "$TAG_SINGLE-$mediaId"
            pendingByTag[tag] = size
            intentSenderDispatcher.requestDelete(
                BinDeletionRequest(
                    tag = tag,
                    intentSender = sender,
                    mediaIds = listOf(mediaId),
                    totalBytes = size
                )
            )
        }
    }

    companion object {
        const val TAG_EMPTY = "empty_bin"
        const val TAG_SINGLE = "single_delete"
    }
}

data class BinUiState(
    val items: List<BinnedMedia> = emptyList(),
    val autoDeleteDays: Int = 30,
    val totalSizeBytes: Long = 0L,
    val showConfirm: Boolean = false,
    val now: Long = 0L
)
