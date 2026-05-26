package com.example.swipeclean.ui.preview

import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.repository.BinRepository
import com.example.swipeclean.domain.repository.KeepFavoriteRepository
import com.example.swipeclean.domain.usecase.GetMediaForPreview
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

@HiltViewModel
class PreviewViewModel @Inject constructor(
    savedStateHandle: SavedStateHandle,
    private val getMediaForPreview: GetMediaForPreview,
    private val binRepository: BinRepository,
    private val keepFavoriteRepository: KeepFavoriteRepository
) : ViewModel() {

    private val mediaId: Long = savedStateHandle.get<Long>("mediaId") ?: -1L

    private val _state = MutableStateFlow(PreviewUiState(loading = true))
    val state: StateFlow<PreviewUiState> = _state.asStateFlow()

    init {
        viewModelScope.launch {
            val item = getMediaForPreview(mediaId)
            _state.value = PreviewUiState(loading = false, item = item)
        }
    }

    fun bin() {
        val item = _state.value.item ?: return
        viewModelScope.launch { binRepository.sendToBin(item) }
    }

    fun keep() {
        val item = _state.value.item ?: return
        viewModelScope.launch { keepFavoriteRepository.keep(item) }
    }

    fun favorite() {
        val item = _state.value.item ?: return
        viewModelScope.launch { keepFavoriteRepository.favorite(item) }
    }
}

data class PreviewUiState(
    val loading: Boolean = true,
    val item: MediaItem? = null
)
