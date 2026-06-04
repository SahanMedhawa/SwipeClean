package com.example.swipeclean.ui.swipe

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.model.AppSettings
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.SettingsRepository
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import com.example.swipeclean.domain.usecase.FavoriteMedia
import com.example.swipeclean.domain.usecase.GetMediaForReview
import com.example.swipeclean.domain.usecase.KeepMedia
import com.example.swipeclean.domain.usecase.SendToBin
import com.example.swipeclean.domain.usecase.UndoLastAction
import com.example.swipeclean.haptics.HapticManager
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.flow.onEach
import kotlinx.coroutines.launch

@HiltViewModel
class SwipeViewModel @Inject constructor(
    private val getMediaForReview: GetMediaForReview,
    private val sendToBinUseCase: SendToBin,
    private val keepMediaUseCase: KeepMedia,
    private val favoriteMediaUseCase: FavoriteMedia,
    private val undoLastActionUseCase: UndoLastAction,
    private val swipeHistoryRepository: SwipeHistoryRepository,
    private val settingsRepository: SettingsRepository,
    val hapticManager: HapticManager
) : ViewModel() {

    private val sessionId: Long = swipeHistoryRepository.startSession()

    private val _state = MutableStateFlow(SwipeUiState(loading = true))
    val state: StateFlow<SwipeUiState> = _state.asStateFlow()

    private var offset: Int = 0
    private var endReached: Boolean = false
    private var loading: Boolean = false

    private var lastUndoableMediaId: Long? = null

    init {
        settingsRepository.settings
            .onEach { settings: AppSettings -> 
                hapticManager.updateSettings(settings) 
                _state.value = _state.value.copy(
                    autoplayVideos = settings.autoplayVideos,
                    muteVideosByDefault = settings.muteVideosByDefault,
                    showSwipeActionBar = settings.showSwipeActionBar
                )
            }
            .launchIn(viewModelScope)

        viewModelScope.launch {
            swipeHistoryRepository.clearOtherSessions(sessionId)
            loadMore()
        }
    }

    fun onSwiped(item: MediaItem, action: SwipeAction) {
        viewModelScope.launch {
            when (action) {
                SwipeAction.BIN -> {
                    sendToBinUseCase(sessionId, item)
                    hapticManager.binThud()
                }
                SwipeAction.KEEP -> {
                    keepMediaUseCase(sessionId, item)
                    hapticManager.keepConfirmation()
                }
                SwipeAction.FAVORITE -> {
                    favoriteMediaUseCase(sessionId, item)
                    hapticManager.favoriteDoublePulse()
                }
            }
            lastUndoableMediaId = item.id

            val current = _state.value
            val updatedQueue = current.queue.drop(1)
            val updatedBinBytes = if (action == SwipeAction.BIN) current.binBytesThisSession + item.sizeBytes
                else current.binBytesThisSession

            _state.value = current.copy(
                queue = updatedQueue,
                reviewedCount = current.reviewedCount + 1,
                binBytesThisSession = updatedBinBytes,
                undoVisible = true,
                undoToken = current.undoToken + 1
            )

            if (updatedQueue.size <= REPLENISH_THRESHOLD && !endReached) {
                loadMore()
            }

            if (updatedQueue.isEmpty() && endReached) {
                _state.value = _state.value.copy(empty = true)
            }
        }
    }

    fun undo() {
        viewModelScope.launch {
            val result = undoLastActionUseCase(sessionId) ?: return@launch
            hapticManager.undoReverseTick()

            // Try to pull the item we just reverted back into the queue, at the head.
            val items = getMediaForReview(offset = 0, limit = 1)
            val restored = items.firstOrNull { it.id == result.mediaId }
                ?: items.firstOrNull()

            val current = _state.value
            val newQueue = if (restored != null) listOf(restored) + current.queue else current.queue
            val newBinBytes = if (result.action == SwipeAction.BIN && restored != null) {
                (current.binBytesThisSession - restored.sizeBytes).coerceAtLeast(0L)
            } else {
                current.binBytesThisSession
            }
            _state.value = current.copy(
                queue = newQueue,
                reviewedCount = (current.reviewedCount - 1).coerceAtLeast(0),
                binBytesThisSession = newBinBytes,
                undoVisible = false,
                empty = false
            )
        }
    }

    fun dismissUndo() {
        _state.value = _state.value.copy(undoVisible = false)
    }

    fun thresholdCross() {
        hapticManager.crossThresholdTick()
    }

    fun cardSnappedBack() {
        hapticManager.snapBackBump()
    }

    private suspend fun loadMore() {
        if (loading || endReached) return
        loading = true
        try {
            val items = getMediaForReview(offset = offset, limit = GetMediaForReview.DEFAULT_LIMIT)
            offset += items.size
            if (items.size < GetMediaForReview.DEFAULT_LIMIT) endReached = true

            val current = _state.value
            val mergedQueue = (current.queue + items).distinctBy { it.id }
            _state.value = current.copy(
                queue = mergedQueue,
                loading = false,
                empty = mergedQueue.isEmpty() && endReached
            )
        } finally {
            loading = false
        }
    }

    companion object {
        private const val REPLENISH_THRESHOLD = 5
    }
}

data class SwipeUiState(
    val loading: Boolean = false,
    val empty: Boolean = false,
    val queue: List<MediaItem> = emptyList(),
    val reviewedCount: Int = 0,
    val binBytesThisSession: Long = 0L,
    val undoVisible: Boolean = false,
    val undoToken: Int = 0,
    val autoplayVideos: Boolean = true,
    val muteVideosByDefault: Boolean = true,
    val showSwipeActionBar: Boolean = true
)
