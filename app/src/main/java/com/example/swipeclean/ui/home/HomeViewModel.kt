package com.example.swipeclean.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.data.media.MediaCounts
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.MediaRepository
import com.example.swipeclean.util.StorageInfoProvider
import com.example.swipeclean.util.StorageSnapshot
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val mediaRepository: MediaRepository,
    private val binRepository: BinRepository,
    private val storageInfoProvider: StorageInfoProvider
) : ViewModel() {

    private val _mediaCounts = MutableStateFlow(MediaCounts(0, 0))
    private val _storage = MutableStateFlow(StorageSnapshot(0L, 0L, 0L))

    val uiState: StateFlow<HomeUiState> = combine(
        binRepository.observeBinCount(),
        binRepository.observeReclaimableBytes(),
        _mediaCounts,
        _storage
    ) { binCount, reclaimable, counts, storage ->
        HomeUiState(
            loading = false,
            photoCount = counts.photos,
            videoCount = counts.videos,
            binCount = binCount,
            reclaimableBytes = reclaimable,
            storage = storage,
            streakDays = 0
        )
    }.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        HomeUiState(loading = true)
    )

    init {
        refresh()
    }

    fun refresh() {
        viewModelScope.launch {
            withContext(Dispatchers.IO) {
                _mediaCounts.value = mediaRepository.counts()
                _storage.value = storageInfoProvider.snapshot()
            }
        }
    }
}

data class HomeUiState(
    val loading: Boolean = false,
    val photoCount: Int = 0,
    val videoCount: Int = 0,
    val binCount: Int = 0,
    val reclaimableBytes: Long = 0L,
    val storage: StorageSnapshot = StorageSnapshot(0L, 0L, 0L),
    val streakDays: Int = 0,
    val duplicatesCount: Int = 0 // Placeholder for future DuplicateDetectionUseCase
)
