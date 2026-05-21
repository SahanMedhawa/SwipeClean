package com.example.swipeclean.domain.usecase

import com.example.swipeclean.domain.model.MediaItem
import com.example.swipeclean.domain.model.SwipeAction
import com.example.swipeclean.domain.repository.AnalyticsRepository
import com.example.swipeclean.domain.repository.KeepFavoriteRepository
import com.example.swipeclean.domain.repository.SwipeHistoryRepository
import javax.inject.Inject

class FavoriteMedia @Inject constructor(
    private val keepFavoriteRepository: KeepFavoriteRepository,
    private val swipeHistoryRepository: SwipeHistoryRepository,
    private val analyticsRepository: AnalyticsRepository
) {
    suspend operator fun invoke(sessionId: Long, item: MediaItem) {
        keepFavoriteRepository.favorite(item)
        swipeHistoryRepository.record(sessionId, item.id, SwipeAction.FAVORITE)
        analyticsRepository.recordSwipe(SwipeAction.FAVORITE, item.folderName, item.sizeBytes)
    }
}
